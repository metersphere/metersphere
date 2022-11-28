package io.metersphere.service;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtIssueCommentMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.AttachmentType;
import io.metersphere.constants.IssueStatus;
import io.metersphere.constants.SystemCustomField;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.IssueExportHeadField;
import io.metersphere.excel.domain.ExcelErrData;
import io.metersphere.excel.domain.ExcelResponse;
import io.metersphere.excel.domain.IssueExcelData;
import io.metersphere.excel.domain.IssueExcelDataFactory;
import io.metersphere.excel.handler.IssueTemplateHeadWriteHandler;
import io.metersphere.excel.listener.IssueExcelListener;
import io.metersphere.excel.utils.EasyExcelExporter;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.plan.dto.PlanReportIssueDTO;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.service.TestPlanTestCaseService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.platform.api.Platform;
import io.metersphere.platform.domain.*;
import io.metersphere.platform.domain.PlatformAttachment;
import io.metersphere.request.IntegrationRequest;
import io.metersphere.xpack.track.dto.AttachmentRequest;
import io.metersphere.request.issues.IssueExportRequest;
import io.metersphere.request.issues.IssueImportRequest;
import io.metersphere.request.issues.PlatformIssueTypeRequest;
import io.metersphere.request.testcase.AuthUserIssueRequest;
import io.metersphere.request.testcase.IssuesCountRequest;
import io.metersphere.service.issue.domain.zentao.ZentaoBuild;
import io.metersphere.service.issue.platform.*;
import io.metersphere.service.remote.project.TrackCustomFieldTemplateService;
import io.metersphere.service.remote.project.TrackIssueTemplateService;
import io.metersphere.service.wapper.TrackProjectService;
import io.metersphere.service.wapper.UserService;
import io.metersphere.utils.DistinctKeyUtil;
import io.metersphere.xpack.track.dto.PlatformStatusDTO;
import io.metersphere.xpack.track.dto.PlatformUser;
import io.metersphere.xpack.track.dto.*;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import io.metersphere.xpack.track.issue.IssuesPlatform;
import jodd.util.CollectionUtil;
import io.metersphere.xpack.track.service.XpackIssueService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesService {

    @Resource
    private BaseIntegrationService baseIntegrationService;
    @Resource
    private TrackProjectService trackProjectService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private TestPlanService testPlanService;
    @Lazy
    @Resource
    private io.metersphere.service.TestCaseService testCaseService;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private TestCaseIssuesMapper testCaseIssuesMapper;
    @Resource
    private ExtIssuesMapper extIssuesMapper;
    @Resource
    private TrackCustomFieldTemplateService trackCustomFieldTemplateService;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    private TrackIssueTemplateService trackIssueTemplateService;
    @Resource
    private TestCaseIssueService testCaseIssueService;
    @Lazy
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private IssueFollowMapper issueFollowMapper;
    @Resource
    private TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    private CustomFieldIssuesService customFieldIssuesService;
    @Resource
    private CustomFieldIssuesMapper customFieldIssuesMapper;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    private AttachmentService attachmentService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private ExtIssueCommentMapper extIssueCommentMapper;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private UserService userService;

    private static final String SYNC_THIRD_PARTY_ISSUES_KEY = "ISSUE:SYNC";

    public void testAuth(String workspaceId, String platform) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setWorkspaceId(workspaceId);
        IssuesPlatform abstractPlatform = IssueFactory.createPlatform(platform, issuesRequest);
        abstractPlatform.testAuth();
    }


    public IssuesWithBLOBs addIssues(IssuesUpdateRequest issuesRequest, List<MultipartFile> files) {
        Project project = baseProjectService.getProjectById(issuesRequest.getProjectId());
        IssuesWithBLOBs issues = null;
        if (PlatformPluginService.isPluginPlatform(project.getPlatform())) {
            PlatformIssuesUpdateRequest platformIssuesUpdateRequest =
                    JSON.parseObject(JSON.toJSONString(issuesRequest), PlatformIssuesUpdateRequest.class);
            List<PlatformCustomFieldItemDTO> customFieldItemDTOS =
                    JSON.parseArray(JSON.toJSONString(issuesRequest.getRequestFields()), PlatformCustomFieldItemDTO.class);
            platformIssuesUpdateRequest.setCustomFieldList(customFieldItemDTOS); // todo 全部插件化后去掉
            platformIssuesUpdateRequest.setUserPlatformUserConfig(userService.getCurrentPlatformInfoStr(SessionUtils.getCurrentWorkspaceId()));
            platformIssuesUpdateRequest.setProjectConfig(PlatformPluginService.getCompatibleProjectConfig(project));

            issues = platformPluginService.getPlatform(project.getPlatform())
                    .addIssue(platformIssuesUpdateRequest);

            insertIssues(issues);
            issuesRequest.setId(issues.getId());
            issues.setPlatform(project.getPlatform());
            // 用例与第三方缺陷平台中的缺陷关联
            handleTestCaseIssues(issuesRequest);

            // 如果是复制新增, 同步MS附件到Jira
            if (StringUtils.isNotEmpty(issuesRequest.getCopyIssueId())) {
                AttachmentRequest attachmentRequest = new AttachmentRequest();
                attachmentRequest.setBelongId(issuesRequest.getCopyIssueId());
                attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
                List<String> attachmentIds = attachmentService.getAttachmentIdsByParam(attachmentRequest);
                if (CollectionUtils.isNotEmpty(attachmentIds)) {
                    for (String attachmentId : attachmentIds) {
                        FileAttachmentMetadata fileAttachmentMetadata = attachmentService.getFileAttachmentMetadataByFileId(attachmentId);
                        File file = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
                        attachmentService.syncIssuesAttachment(issues, file, AttachmentSyncType.UPLOAD);
                    }
                }
            }
        } else {
            List<IssuesPlatform> platformList = getAddPlatforms(issuesRequest);
            for (IssuesPlatform platform : platformList) {
                issues = platform.addIssue(issuesRequest);
            }
        }

        if (issuesRequest.getIsPlanEdit()) {
            issuesRequest.getAddResourceIds().forEach(l -> {
                testCaseIssueService.updateIssuesCount(l);
            });
        }
        String issuesId = issues.getId();
        saveFollows(issuesId, issuesRequest.getFollows());
        customFieldIssuesService.addFields(issuesId, issuesRequest.getAddFields());
        customFieldIssuesService.editFields(issuesId, issuesRequest.getEditFields());
        if (StringUtils.isNotEmpty(issuesRequest.getCopyIssueId())) {
            final String platformId = issues.getPlatformId();
            // 复制新增, 同步缺陷的MS附件
            AttachmentRequest attachmentRequest = new AttachmentRequest();
            attachmentRequest.setCopyBelongId(issuesRequest.getCopyIssueId());
            attachmentRequest.setBelongId(issues.getId());
            attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
            attachmentService.copyAttachment(attachmentRequest);

            // MS附件同步到其他平台, Jira, Zentao已经在创建缺陷时处理, AzureDevops单独处理
            if (StringUtils.equals(issuesRequest.getPlatform(), IssuesManagePlatform.AzureDevops.toString())) {
                AttachmentRequest request = new AttachmentRequest();
                request.setBelongId(issuesRequest.getCopyIssueId());
                request.setBelongType(AttachmentType.ISSUE.type());
                uploadAzureCopyAttachment(request, issuesRequest.getPlatform(), platformId);
            }
        } else {
            final String issueId = issues.getId();
            final String platform = issues.getPlatform();
            // 新增, 需保存并同步所有待上传的附件
            if (CollectionUtils.isNotEmpty(files)) {
                files.forEach(file -> {
                    AttachmentRequest attachmentRequest = new AttachmentRequest();
                    attachmentRequest.setBelongId(issueId);
                    attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
                    attachmentService.uploadAttachment(attachmentRequest, file);
                });
            }
            // 处理待关联的文件附件, 生成关联记录, 并同步至第三方平台
            if (CollectionUtils.isNotEmpty(issuesRequest.getRelateFileMetaIds())) {
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                FileAssociationMapper associationBatchMapper = sqlSession.getMapper(FileAssociationMapper.class);
                AttachmentModuleRelationMapper attachmentModuleRelationBatchMapper = sqlSession.getMapper(AttachmentModuleRelationMapper.class);
                FileAttachmentMetadataMapper fileAttachmentMetadataBatchMapper = sqlSession.getMapper(FileAttachmentMetadataMapper.class);
                issuesRequest.getRelateFileMetaIds().forEach(filemetaId -> {
                    FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(filemetaId);
                    FileAssociation fileAssociation = new FileAssociation();
                    fileAssociation.setId(UUID.randomUUID().toString());
                    fileAssociation.setFileMetadataId(filemetaId);
                    fileAssociation.setFileType(fileMetadata.getType());
                    fileAssociation.setType(FileAssociationType.ISSUE.name());
                    fileAssociation.setProjectId(fileMetadata.getProjectId());
                    fileAssociation.setSourceItemId(filemetaId);
                    fileAssociation.setSourceId(issueId);
                    associationBatchMapper.insert(fileAssociation);
                    AttachmentModuleRelation relation = new AttachmentModuleRelation();
                    relation.setRelationId(issueId);
                    relation.setRelationType(AttachmentType.ISSUE.type());
                    relation.setFileMetadataRefId(fileAssociation.getId());
                    relation.setAttachmentId(UUID.randomUUID().toString());
                    attachmentModuleRelationBatchMapper.insert(relation);
                    FileAttachmentMetadata fileAttachmentMetadata = new FileAttachmentMetadata();
                    BeanUtils.copyBean(fileAttachmentMetadata, fileMetadata);
                    fileAttachmentMetadata.setId(relation.getAttachmentId());
                    fileAttachmentMetadata.setCreator(fileMetadata.getCreateUser() == null ? StringUtils.EMPTY : fileMetadata.getCreateUser());
                    fileAttachmentMetadata.setFilePath(fileMetadata.getPath() == null ? StringUtils.EMPTY : fileMetadata.getPath());
                    fileAttachmentMetadataBatchMapper.insert(fileAttachmentMetadata);
                    // 下载文件管理文件, 同步到第三方平台
                    File refFile = attachmentService.downloadMetadataFile(filemetaId, fileMetadata.getName());
                    if (PlatformPluginService.isPluginPlatform(platform)) {
                        issuesRequest.setPlatform(platform);
                        attachmentService.syncIssuesAttachment(issuesRequest, refFile, AttachmentSyncType.UPLOAD);
                    } else {
                        IssuesRequest addIssueRequest = new IssuesRequest();
                        addIssueRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                        addIssueRequest.setProjectId(SessionUtils.getCurrentProjectId());
                        Objects.requireNonNull(IssueFactory.createPlatform(platform, addIssueRequest))
                                .syncIssuesAttachment(issuesRequest, refFile, AttachmentSyncType.UPLOAD);
                    }
                    FileUtils.deleteFile(FileUtils.ATTACHMENT_TMP_DIR + File.separator + fileMetadata.getName());
                });
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
        return getIssue(issues.getId());
    }

    protected IssuesWithBLOBs insertIssues(IssuesWithBLOBs issues) {
        if (StringUtils.isBlank(issues.getId())) {
            issues.setId(UUID.randomUUID().toString());
        }
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setNum(getNextNum(issues.getProjectId()));
        issues.setCreator(SessionUtils.getUserId());
        issuesMapper.insert(issues);
        return issues;
    }

    protected int getNextNum(String projectId) {
        Issues issue = extIssuesMapper.getNextNum(projectId);
        if (issue == null || issue.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(issue.getNum() + 1).orElse(100001);
        }
    }

    public void handleTestCaseIssues(IssuesUpdateRequest issuesRequest) {
        String issuesId = issuesRequest.getId();
        List<String> deleteCaseIds = issuesRequest.getDeleteResourceIds();

        if (!org.springframework.util.CollectionUtils.isEmpty(deleteCaseIds)) {
            TestCaseIssuesExample example = new TestCaseIssuesExample();
            example.createCriteria()
                    .andResourceIdIn(deleteCaseIds)
                    .andIssuesIdEqualTo(issuesId);
            // 测试计划的用例 deleteCaseIds 是空的， 不会进到这里
            example.or(
                    example.createCriteria()
                            .andRefIdIn(deleteCaseIds)
                            .andIssuesIdEqualTo(issuesId)
            );
            testCaseIssuesMapper.deleteByExample(example);
        }

        List<String> addCaseIds = issuesRequest.getAddResourceIds();

        if (!org.springframework.util.CollectionUtils.isEmpty(addCaseIds)) {
            if (issuesRequest.getIsPlanEdit()) {
                addCaseIds.forEach(caseId -> {
                    testCaseIssueService.add(issuesId, caseId, issuesRequest.getRefId(), IssueRefType.PLAN_FUNCTIONAL.name());
                    testCaseIssueService.updateIssuesCount(caseId);
                });
            } else {
                addCaseIds.forEach(caseId -> testCaseIssueService.add(issuesId, caseId, null, IssueRefType.FUNCTIONAL.name()));
            }
        }
    }

    public IssuesWithBLOBs updateIssues(IssuesUpdateRequest issuesRequest) {
        PlatformIssuesUpdateRequest platformIssuesUpdateRequest = JSON.parseObject(JSON.toJSONString(issuesRequest), PlatformIssuesUpdateRequest.class);
        Project project = baseProjectService.getProjectById(issuesRequest.getProjectId());
        if (PlatformPluginService.isPluginPlatform(project.getPlatform())) {

            Platform platform = platformPluginService.getPlatform(project.getPlatform());

            if (platform.isAttachmentUploadSupport()) {
                AttachmentRequest attachmentRequest = new AttachmentRequest();
                attachmentRequest.setBelongId(issuesRequest.getId());
                attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
                List<FileAttachmentMetadata> fileAttachmentMetadata = attachmentService.listMetadata(attachmentRequest);
                Set<String> msAttachmentNames = fileAttachmentMetadata.stream()
                        .map(FileAttachmentMetadata::getName)
                        .collect(Collectors.toSet());
                // 获得缺陷MS附件名称
                platformIssuesUpdateRequest.setMsAttachmentNames(msAttachmentNames);
            }

            List<PlatformCustomFieldItemDTO> customFieldItemDTOS = JSON.parseArray(JSON.toJSONString(issuesRequest.getRequestFields()), PlatformCustomFieldItemDTO.class);
            platformIssuesUpdateRequest.setCustomFieldList(customFieldItemDTOS); // todo 全部插件化后去掉
            platformIssuesUpdateRequest.setUserPlatformUserConfig(userService.getCurrentPlatformInfoStr(SessionUtils.getCurrentWorkspaceId()));
            platformIssuesUpdateRequest.setProjectConfig(PlatformPluginService.getCompatibleProjectConfig(project));
            IssuesWithBLOBs issue = platformPluginService.getPlatform(project.getPlatform())
                    .updateIssue(platformIssuesUpdateRequest);

            issue.setUpdateTime(System.currentTimeMillis());
            issuesMapper.updateByPrimaryKeySelective(issue);
            handleTestCaseIssues(issuesRequest);
        } else {
            List<IssuesPlatform> platformList = getUpdatePlatforms(issuesRequest);
            platformList.forEach(platform -> {
                platform.updateIssue(issuesRequest);
            });
        }

        customFieldIssuesService.editFields(issuesRequest.getId(), issuesRequest.getEditFields());
        customFieldIssuesService.addFields(issuesRequest.getId(), issuesRequest.getAddFields());

        return getIssue(issuesRequest.getId());
    }

    public void saveFollows(String issueId, List<String> follows) {
        IssueFollowExample example = new IssueFollowExample();
        example.createCriteria().andIssueIdEqualTo(issueId);
        issueFollowMapper.deleteByExample(example);
        if (!CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                IssueFollow issueFollow = new IssueFollow();
                issueFollow.setIssueId(issueId);
                issueFollow.setFollowId(follow);
                issueFollowMapper.insert(issueFollow);
            }
        }
    }

    public List<IssuesPlatform> getAddPlatforms(IssuesUpdateRequest updateRequest) {
        List<String> platforms = new ArrayList<>();
        // 缺陷管理关联
        platforms.add(getPlatform(updateRequest.getProjectId()));

        if (CollectionUtils.isEmpty(platforms)) {
            platforms.add(IssuesManagePlatform.Local.toString());
        }
        IssuesRequest issuesRequest = new IssuesRequest();
        BeanUtils.copyBean(issuesRequest, updateRequest);
        return IssueFactory.createPlatforms(platforms, issuesRequest);
    }

    public List<IssuesPlatform> getUpdatePlatforms(IssuesUpdateRequest updateRequest) {
        String id = updateRequest.getId();
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        String platform = issuesWithBLOBs.getPlatform();
        List<String> platforms = new ArrayList<>();
        if (StringUtils.isBlank(platform)) {
            platforms.add(IssuesManagePlatform.Local.toString());
        } else {
            platforms.add(platform);
        }
        IssuesRequest issuesRequest = new IssuesRequest();
        BeanUtils.copyBean(issuesRequest, updateRequest);
        return IssueFactory.createPlatforms(platforms, issuesRequest);
    }

    public List<IssuesDao> getIssues(String caseResourceId, String refType) {
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setCaseResourceId(caseResourceId);
        ServiceUtils.getDefaultOrder(issueRequest.getOrders());
        issueRequest.setRefType(refType);
        List<IssuesDao> issues = extIssuesMapper.getIssuesByCaseId(issueRequest);
        handleCustomFieldStatus(issues);
        return DistinctKeyUtil.distinctByKey(issues, IssuesDao::getId);
    }

    private void handleCustomFieldStatus(List<IssuesDao> issues) {
        if (CollectionUtils.isEmpty(issues)) {
            return;
        }
        List<String> issueIds = issues.stream().map(Issues::getId).collect(Collectors.toList());
        String projectId = issues.get(0).getProjectId();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        String templateId = project.getIssueTemplateId();
        if (StringUtils.isBlank(templateId)) {
            return;
        }
        // 模版对于同一个系统字段应该只关联一次
        List<CustomFieldDao> customFields = trackCustomFieldTemplateService.getCustomFieldByTemplateId(templateId);
        List<String> fieldIds = customFields.stream()
                .filter(customField -> StringUtils.equals(SystemCustomField.ISSUE_STATUS, customField.getName()))
                .map(CustomFieldDao::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fieldIds)) {
            return;
        }
        // 该系统字段的自定义ID
        String customFieldId = fieldIds.get(0);
        CustomFieldIssuesExample example = new CustomFieldIssuesExample();
        example.createCriteria().andFieldIdEqualTo(customFieldId).andResourceIdIn(issueIds);
        List<CustomFieldIssues> customFieldIssues = customFieldIssuesMapper.selectByExample(example);
        Map<String, String> statusMap = customFieldIssues.stream().collect(Collectors.toMap(CustomFieldIssues::getResourceId, CustomFieldIssues::getValue));
        if (MapUtils.isEmpty(statusMap)) {
            return;
        }
        for (IssuesDao issue : issues) {
            issue.setStatus(statusMap.getOrDefault(issue.getId(), StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY));
        }
    }

    public IssuesWithBLOBs getIssue(String id) {
        IssuesDao issuesWithBLOBs = extIssuesMapper.selectByPrimaryKey(id);
        if (issuesWithBLOBs == null) {
            return null;
        }
        IssuesRequest issuesRequest = new IssuesRequest();
        Project project = baseProjectService.getProjectById(issuesWithBLOBs.getProjectId());
        issuesRequest.setWorkspaceId(project.getWorkspaceId());
        issuesRequest.setProjectId(issuesWithBLOBs.getProjectId());
        issuesRequest.setUserId(issuesWithBLOBs.getCreator());
        if (StringUtils.equals(issuesWithBLOBs.getPlatform(), IssuesManagePlatform.Tapd.name())) {
            TapdPlatform tapdPlatform = (TapdPlatform) IssueFactory.createPlatform(IssuesManagePlatform.Tapd.name(), issuesRequest);
            List<String> tapdUsers = tapdPlatform.getTapdUsers(issuesWithBLOBs.getProjectId(), issuesWithBLOBs.getPlatformId());
            issuesWithBLOBs.setTapdUsers(tapdUsers);
        }
        if (StringUtils.equals(issuesWithBLOBs.getPlatform(), IssuesManagePlatform.Zentao.name())) {
            ZentaoPlatform zentaoPlatform = (ZentaoPlatform) IssueFactory.createPlatform(IssuesManagePlatform.Zentao.name(), issuesRequest);
            zentaoPlatform.getZentaoAssignedAndBuilds(issuesWithBLOBs);
        }
        buildCustomField(issuesWithBLOBs);
        return issuesWithBLOBs;
    }

    public String getPlatform(String projectId) {
        Project project = baseProjectService.getProjectById(projectId);
        return project.getPlatform();
    }

    public List<String> getPlatforms(Project project) {
        String workspaceId = project.getWorkspaceId();
        boolean tapd = isIntegratedPlatform(workspaceId, IssuesManagePlatform.Tapd.toString());
        boolean jira = isIntegratedPlatform(workspaceId, IssuesManagePlatform.Jira.toString());
        boolean zentao = isIntegratedPlatform(workspaceId, IssuesManagePlatform.Zentao.toString());
        boolean azure = isIntegratedPlatform(workspaceId, IssuesManagePlatform.AzureDevops.toString());

        List<String> platforms = new ArrayList<>();
        if (tapd) {
            // 是否关联了项目
            String tapdId = project.getTapdId();
            if (StringUtils.isNotBlank(tapdId) && StringUtils.equals(project.getPlatform(), IssuesManagePlatform.Tapd.toString())) {
                platforms.add(IssuesManagePlatform.Tapd.name());
            }

        }

        if (jira) {
            String jiraKey = project.getJiraKey();
            if (StringUtils.isNotBlank(jiraKey) && PlatformPluginService.isPluginPlatform(project.getPlatform())) {
                platforms.add(IssuesManagePlatform.Jira.name());
            }
        }

        if (zentao) {
            String zentaoId = project.getZentaoId();
            if (StringUtils.isNotBlank(zentaoId) && StringUtils.equals(project.getPlatform(), IssuesManagePlatform.Zentao.toString())) {
                platforms.add(IssuesManagePlatform.Zentao.name());
            }
        }

        if (azure) {
            String azureDevopsId = project.getAzureDevopsId();
            if (StringUtils.isNotBlank(azureDevopsId) && StringUtils.equals(project.getPlatform(), IssuesManagePlatform.AzureDevops.toString())) {
                platforms.add(IssuesManagePlatform.AzureDevops.name());
            }
        }

        return platforms;
    }


    /**
     * 是否关联平台
     */
    public boolean isIntegratedPlatform(String workspaceId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        request.setWorkspaceId(workspaceId);
        ServiceIntegration integration = baseIntegrationService.get(request);
        return StringUtils.isNotBlank(integration.getId());
    }

    public void closeLocalIssue(String issueId) {
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        issues.setId(issueId);
        issues.setStatus("closed");
        issuesMapper.updateByPrimaryKeySelective(issues);
    }

    public List<PlatformUser> getTapdProjectUsers(IssuesRequest request) {
        IssuesPlatform platform = IssueFactory.createPlatform(IssuesManagePlatform.Tapd.name(), request);
        return platform.getPlatformUser();
    }

    public List<PlatformUser> getZentaoUsers(IssuesRequest request) {
        IssuesPlatform platform = IssueFactory.createPlatform(IssuesManagePlatform.Zentao.name(), request);
        return platform.getPlatformUser();
    }

    public void deleteIssue(String id) {
        issuesMapper.deleteByPrimaryKey(id);
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria().andIssuesIdEqualTo(id);
        List<TestCaseIssues> testCaseIssues = testCaseIssuesMapper.selectByExample(example);
        testCaseIssues.forEach(i -> {
            if (i.getRefType().equals(IssueRefType.PLAN_FUNCTIONAL.name())) {
                testCaseIssueService.updateIssuesCount(i.getResourceId());
            }
        });
        customFieldIssuesService.deleteByResourceId(id);
        testCaseIssuesMapper.deleteByExample(example);
    }

    public void deleteIssueRelate(IssuesRequest request) {
        String caseResourceId = request.getCaseResourceId();
        String id = request.getId();
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        if (request.getIsPlanEdit() == true) {
            example.createCriteria().andResourceIdEqualTo(caseResourceId).andIssuesIdEqualTo(id);
            testCaseIssuesMapper.deleteByExample(example);
            testCaseIssueService.updateIssuesCount(caseResourceId);
        } else {
            IssuesUpdateRequest updateRequest = new IssuesUpdateRequest();
            updateRequest.setId(request.getId());
            updateRequest.setResourceId(request.getCaseResourceId());
            updateRequest.setProjectId(request.getProjectId());
            updateRequest.setWorkspaceId(request.getWorkspaceId());
            List<IssuesPlatform> platformList = getUpdatePlatforms(updateRequest);
            platformList.forEach(platform -> {
                platform.removeIssueParentLink(updateRequest);
            });

            extIssuesMapper.deleteIssues(id, caseResourceId);
            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria().andCaseIdEqualTo(caseResourceId);
            List<TestPlanTestCase> list = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);
            list.forEach(item -> {
                testCaseIssueService.updateIssuesCount(item.getId());
            });
        }
    }

    public void delete(String id) {
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        List platforms = new ArrayList<>();
        platforms.add(issuesWithBLOBs.getPlatform());
        String projectId = issuesWithBLOBs.getProjectId();
        Project project = baseProjectService.getProjectById(projectId);
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setWorkspaceId(project.getWorkspaceId());
        if (PlatformPluginService.isPluginPlatform(issuesWithBLOBs.getPlatform())) {
            platformPluginService.getPlatform(issuesWithBLOBs.getPlatform())
                    .deleteIssue(issuesWithBLOBs.getPlatformId());
            deleteIssue(id);
        } else {
            IssuesPlatform platform = IssueFactory.createPlatform(issuesWithBLOBs.getPlatform(), issuesRequest);
            platform.deleteIssue(id);
        }

        // 删除缺陷对应的附件
        AttachmentRequest request = new AttachmentRequest();
        request.setBelongId(id);
        request.setBelongType(AttachmentType.ISSUE.type());
        attachmentService.deleteAttachment(request);
    }

    public void batchDelete(IssuesUpdateRequest request) {
        if (request.getBatchDeleteAll()) {
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
            issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
            List<IssuesDao> issuesDaos = listByWorkspaceId(issuesRequest);
            if (CollectionUtils.isNotEmpty(issuesDaos)) {
                issuesDaos.parallelStream().forEach(issuesDao -> {
                    delete(issuesDao.getId());
                });
            }
        } else {
            if (CollectionUtils.isNotEmpty(request.getBatchDeleteIds())) {
                request.getBatchDeleteIds().parallelStream().forEach(id -> delete(id));
            }
        }
    }

    public List<ZentaoBuild> getZentaoBuilds(IssuesRequest request) {
        try {
            ZentaoPlatform platform = (ZentaoPlatform) IssueFactory.createPlatform(IssuesManagePlatform.Zentao.name(), request);
            return platform.getBuilds();
        } catch (Exception e) {
            LogUtil.error("get zentao builds fail.");
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("zentao_get_project_builds_fail"));
        }
        return null;
    }

    public List<IssuesDao> list(IssuesRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrderByField(request.getOrders(), "create_time"));
        request.getOrders().forEach(order -> {
            if (StringUtils.isNotEmpty(order.getName()) && order.getName().startsWith("custom")) {
                request.setIsCustomSorted(true);
                request.setCustomFieldId(order.getName().replace("custom_", StringUtils.EMPTY));
                order.setPrefix("cfi");
                order.setName("value");
            }
        });
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<IssuesDao> issues = extIssuesMapper.getIssues(request);

        Map<String, Set<String>> caseSetMap = getCaseSetMap(issues);
        Map<String, User> userMap = getUserMap(issues);
        Map<String, String> planMap = getPlanMap(issues);

        issues.forEach(item -> {
            User createUser = userMap.get(item.getCreator());
            if (createUser != null) {
                item.setCreatorName(createUser.getName());
            }
            String resourceName = planMap.get(item.getResourceId());
            if (StringUtils.isNotBlank(resourceName)) {
                item.setResourceName(resourceName);
            }

            Set<String> caseIdSet = caseSetMap.get(item.getId());
            if (caseIdSet == null) {
                caseIdSet = new HashSet<>();
            }
            item.setCaseIds(new ArrayList<>(caseIdSet));
            item.setCaseCount(caseIdSet.size());
        });
        buildCustomField(issues);
        //处理MD图片链接内容
        handleJiraIssueMdUrl(request.getWorkspaceId(), request.getProjectId(), issues);
        return issues;
    }

    private void buildCustomField(List<IssuesDao> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Map<String, List<CustomFieldDao>> fieldMap =
                customFieldIssuesService.getMapByResourceIds(data.stream().map(IssuesDao::getId).collect(Collectors.toList()));
        data.forEach(i -> i.setFields(fieldMap.get(i.getId())));
    }

    private void buildCustomField(IssuesDao data) {
        CustomFieldIssuesExample example = new CustomFieldIssuesExample();
        example.createCriteria().andResourceIdEqualTo(data.getId());
        List<CustomFieldIssues> customFieldTestCases = customFieldIssuesMapper.selectByExample(example);
        List<CustomFieldDao> fields = new ArrayList<>();
        customFieldTestCases.forEach(i -> {
            CustomFieldDao customFieldDao = new CustomFieldDao();
            customFieldDao.setId(i.getFieldId());
            customFieldDao.setValue(i.getValue());
            customFieldDao.setTextValue(i.getTextValue());
            fields.add(customFieldDao);
        });
        data.setFields(fields);
    }

    private void buildCustomField(List<IssuesDao> data, Boolean isThirdTemplate, List<CustomFieldDao> customFields) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        Map<String, List<CustomFieldDao>> fieldMap =
                customFieldIssuesService.getMapByResourceIds(data.stream().map(IssuesDao::getId).collect(Collectors.toList()));
        try {
            Map<String, CustomField> fieldMaps = new HashMap<>();
            if (isThirdTemplate) {
                fieldMaps = customFields.stream().collect(Collectors.toMap(CustomFieldDao::getId, field -> (CustomField) field));
            } else {
                List<CustomFieldDao> customfields = fieldMap.get(data.get(0).getId());
                if (CollectionUtils.isNotEmpty(customfields) && customfields.size() > 0) {
                    List<String> ids = customfields.stream().map(CustomFieldDao::getId).collect(Collectors.toList());
                    List<CustomField> issueFields = baseCustomFieldService.getFieldByIds(ids);
                    fieldMaps = issueFields.stream().collect(Collectors.toMap(CustomField::getId, field -> field));
                }
            }

            for (Map.Entry<String, List<CustomFieldDao>> entry : fieldMap.entrySet()) {
                for (CustomFieldDao fieldDao : entry.getValue()) {
                    CustomField customField = fieldMaps.get(fieldDao.getId());
                    if (customField != null) {
                        fieldDao.setName(customField.getName());
                        if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.RICH_TEXT.getValue(), CustomFieldType.TEXTAREA.getValue())) {
                            fieldDao.setValue(fieldDao.getTextValue());
                        }
                        if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.DATE.getValue()) && StringUtils.isNotEmpty(fieldDao.getValue()) && !StringUtils.equals(fieldDao.getValue(), "null")) {
                            Date date = DateUtils.parseDate(fieldDao.getValue().replaceAll("\"", StringUtils.EMPTY), "yyyy-MM-dd");
                            String format = DateUtils.format(date, "yyyy/MM/dd");
                            fieldDao.setValue("\"" + format + "\"");
                        }
                        if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.DATETIME.getValue()) && StringUtils.isNotEmpty(fieldDao.getValue()) && !StringUtils.equals(fieldDao.getValue(), "null")) {
                            Date date = null;
                            if (fieldDao.getValue().contains("T") && fieldDao.getValue().length() == 18) {
                                date = DateUtils.parseDate(fieldDao.getValue().replaceAll("\"", StringUtils.EMPTY), "yyyy-MM-dd'T'HH:mm");
                            } else if (fieldDao.getValue().contains("T") && fieldDao.getValue().length() == 21) {
                                date = DateUtils.parseDate(fieldDao.getValue().replaceAll("\"", StringUtils.EMPTY), "yyyy-MM-dd'T'HH:mm:ss");
                            } else if (fieldDao.getValue().contains("T") && fieldDao.getValue().length() > 21) {
                                date = DateUtils.parseDate(fieldDao.getValue().replaceAll("\"", StringUtils.EMPTY).substring(0, 19), "yyyy-MM-dd'T'HH:mm:ss");
                            } else {
                                date = DateUtils.parseDate(fieldDao.getValue().replaceAll("\"", StringUtils.EMPTY));
                            }
                            String format = DateUtils.format(date, "yyyy/MM/dd HH:mm:ss");
                            fieldDao.setValue("\"" + format + "\"");
                        }
                        if (StringUtils.equalsAnyIgnoreCase(customField.getType(), CustomFieldType.SELECT.getValue(),
                                CustomFieldType.MULTIPLE_SELECT.getValue(), CustomFieldType.CHECKBOX.getValue(), CustomFieldType.RADIO.getValue())
                                && !StringUtils.equalsAnyIgnoreCase(customField.getName(), SystemCustomField.ISSUE_STATUS)) {
                            fieldDao.setValue(parseOptionValue(customField.getOptions(), fieldDao.getValue()));
                        }
                    }
                }
            }

            data.forEach(i -> i.setFields(fieldMap.get(i.getId())));
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
        }

    }

    private void handleJiraIssueMdUrl(String workPlaceId, String projectId, List<IssuesDao> issues) {
        issues.forEach(issue -> {
            if (StringUtils.isNotEmpty(issue.getDescription()) && issue.getDescription().contains("platform=Jira&")) {
                issue.setDescription(replaceJiraMdUrlParam(issue.getDescription(), workPlaceId, projectId));
            }
            if (StringUtils.isNotEmpty(issue.getCustomFields()) && issue.getCustomFields().contains("platform=Jira&")) {
                issue.setCustomFields(replaceJiraMdUrlParam(issue.getCustomFields(), workPlaceId, projectId));
            }
            if (CollectionUtils.isNotEmpty(issue.getFields())) {
                issue.getFields().forEach(field -> {
                    if (StringUtils.isNotEmpty(field.getTextValue()) && field.getTextValue().contains("platform=Jira&")) {
                        field.setTextValue(replaceJiraMdUrlParam(field.getTextValue(), workPlaceId, projectId));
                    }
                    if (StringUtils.isNotEmpty(field.getValue()) && field.getValue().contains("platform=Jira&")) {
                        field.setValue(replaceJiraMdUrlParam(field.getValue(), workPlaceId, projectId));
                    }
                });
            }
        });
    }

    private String replaceJiraMdUrlParam(String url, String workspaceId, String projectId) {
        if (url.contains("&workspace_id=")) {
            return url;
        }
        return url.replaceAll("platform=Jira&",
                "platform=Jira&workspace_id=" + workspaceId + "&");
    }

    private Map<String, List<IssueCommentDTO>> getCommentMap(List<IssuesDao> issues) {
        List<String> issueIds = issues.stream().map(IssuesDao::getId).collect(Collectors.toList());
        List<IssueCommentDTO> comments = extIssueCommentMapper.getCommentsByIssueIds(issueIds);
        Map<String, List<IssueCommentDTO>> commentMap = comments.stream().collect(Collectors.groupingBy(IssueCommentDTO::getIssueId));
        return commentMap;
    }

    private Map<String, String> getPlanMap(List<IssuesDao> issues) {
        List<String> resourceIds = issues.stream().map(IssuesDao::getResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<TestPlan> testPlans = testPlanService.getTestPlanByIds(resourceIds);
        Map<String, String> planMap = new HashMap<>();
        if (testPlans != null) {
            planMap = testPlans.stream()
                    .collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));
        }
        return planMap;
    }

    private Map<String, User> getUserMap(List<IssuesDao> issues) {
        List<String> userIds = issues.stream()
                .map(IssuesDao::getCreator)
                .collect(Collectors.toList());
        return ServiceUtils.getUserMap(userIds);
    }

    private Map<String, Set<String>> getCaseSetMap(List<IssuesDao> issues) {
        List<String> ids = issues.stream().map(Issues::getId).collect(Collectors.toList());
        Map<String, Set<String>> map = new HashMap<>();
        if (ids.size() == 0) {
            return map;
        }
        TestCaseIssuesExample example = new TestCaseIssuesExample();
        example.createCriteria()
                .andIssuesIdIn(ids);
        List<TestCaseIssues> testCaseIssues = testCaseIssuesMapper.selectByExample(example);

        List<String> caseIds = testCaseIssues.stream().map(x ->
                x.getRefType().equals(IssueRefType.PLAN_FUNCTIONAL.name()) ? x.getRefId() : x.getResourceId())
                .collect(Collectors.toList());

        List<TestCaseDTO> notInTrashCase = testCaseService.getTestCaseByIds(caseIds);

        if (CollectionUtils.isNotEmpty(notInTrashCase)) {
            Set<String> notInTrashCaseSet = notInTrashCase.stream()
                    .map(TestCaseDTO::getId)
                    .collect(Collectors.toSet());

            testCaseIssues.forEach(i -> {
                Set<String> caseIdSet = new HashSet<>();
                String caseId = i.getRefType().equals(IssueRefType.PLAN_FUNCTIONAL.name()) ? i.getRefId() : i.getResourceId();
                if (notInTrashCaseSet.contains(caseId)) {
                    caseIdSet.add(caseId);
                }
                if (map.get(i.getIssuesId()) != null) {
                    map.get(i.getIssuesId()).addAll(caseIdSet);
                } else {
                    map.put(i.getIssuesId(), caseIdSet);
                }
            });
        }
        return map;
    }

    public Map<String, List<IssuesDao>> getIssueMap(List<IssuesDao> issues) {
        Map<String, List<IssuesDao>> issueMap = new HashMap<>();
        issues.forEach(item -> {
            String platForm = item.getPlatform();
            if (StringUtils.equalsIgnoreCase(IssuesManagePlatform.Local.toString(), item.getPlatform())) {
                // 可能有大小写的问题
                platForm = IssuesManagePlatform.Local.toString();
            }
            List<IssuesDao> issuesDao = issueMap.get(platForm);
            if (issuesDao == null) {
                issuesDao = new ArrayList<>();
            }
            issuesDao.add(item);
            issueMap.put(platForm, issuesDao);
        });
        return issueMap;
    }

    public void syncThirdPartyIssues() {
        List<String> projectIds = trackProjectService.getThirdPartProjectIds();
        projectIds.forEach(id -> {
            try {
                syncThirdPartyIssues(id);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
    }

    public void issuesCount() {
        LogUtil.info("测试计划-测试用例同步缺陷信息开始");
        int pageSize = 100;
        int pages = 1;
        for (int i = 0; i < pages; i++) {
            Page<List<TestPlanTestCase>> page = PageHelper.startPage(i, pageSize, true);
            List<TestPlanTestCaseWithBLOBs> list = testPlanTestCaseService.listAll();
            pages = page.getPages();// 替换成真实的值
            list.forEach(l -> {
                testCaseIssueService.updateIssuesCount(l.getCaseId());
            });
        }
        LogUtil.info("测试计划-测试用例同步缺陷信息结束");
    }

    public boolean checkSync(String projectId) {
        String syncValue = getSyncKey(projectId);
        if (StringUtils.isNotEmpty(syncValue)) {
            return false;
        }
        return true;
    }

    public String getSyncKey(String projectId) {
        return stringRedisTemplate.opsForValue().get(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId);
    }

    public void setSyncKey(String projectId) {
        stringRedisTemplate.opsForValue().set(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId,
                UUID.randomUUID().toString(), 60 * 10, TimeUnit.SECONDS);
    }

    public void deleteSyncKey(String projectId) {
        stringRedisTemplate.delete(SYNC_THIRD_PARTY_ISSUES_KEY + ":" + projectId);
    }

    public boolean syncThirdPartyIssues(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            String syncValue = getSyncKey(projectId);
            if (StringUtils.isNotEmpty(syncValue)) {
                return false;
            }

            setSyncKey(projectId);

            Project project = baseProjectService.getProjectById(projectId);
            List<IssuesDao> issues = extIssuesMapper.getIssueForSync(projectId, project.getPlatform());

            if (CollectionUtils.isEmpty(issues)) {
                deleteSyncKey(projectId);
                return true;
            }

            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(projectId);
            issuesRequest.setWorkspaceId(project.getWorkspaceId());

            try {
                if (!trackProjectService.isThirdPartTemplate(project)) {
                    String defaultCustomFields = getDefaultCustomFields(projectId);
                    issuesRequest.setDefaultCustomFields(defaultCustomFields);
                }
                if (PlatformPluginService.isPluginPlatform(project.getPlatform())) {
                    // 分批处理
                    SubListUtil.dealForSubList(issues, 500, (subIssue) ->
                            syncPluginThirdPartyIssues(subIssue, project, issuesRequest.getDefaultCustomFields()));
                } else {
                    IssuesPlatform platform = IssueFactory.createPlatform(project.getPlatform(), issuesRequest);
                    syncThirdPartyIssues(platform::syncIssues, project, issues);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                deleteSyncKey(projectId);
            }
        }
        return true;
    }

    public void syncPluginThirdPartyIssues(List<IssuesDao> issues, Project project, String defaultCustomFields) {
        List<PlatformIssuesDTO> platformIssues = JSON.parseArray(JSON.toJSONString(issues), PlatformIssuesDTO.class);
        platformIssues.stream().forEach(item -> {
            // 给缺陷添加自定义字段
            List<PlatformCustomFieldItemDTO> platformCustomFieldList = extIssuesMapper.getIssueCustomField(item.getId()).stream()
                    .map(field -> {
                        PlatformCustomFieldItemDTO platformCustomFieldItemDTO = new PlatformCustomFieldItemDTO();
                        BeanUtils.copyBean(platformCustomFieldItemDTO, field);
                        return platformCustomFieldItemDTO;
                    })
                    .collect(Collectors.toList());
            item.setCustomFieldList(platformCustomFieldList);
        });
        SyncIssuesRequest request = new SyncIssuesRequest();
        request.setIssues(platformIssues);
        request.setDefaultCustomFields(defaultCustomFields);
        request.setProjectConfig(PlatformPluginService.getCompatibleProjectConfig(project));
        Platform platform = platformPluginService.getPlatform(project.getPlatform(), project.getWorkspaceId());

        // 获取需要变更的缺陷
        SyncIssuesResult syncIssuesResult = platform.syncIssues(request);
        List<IssuesWithBLOBs> updateIssues = syncIssuesResult.getUpdateIssues();

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            IssuesMapper issueBatchMapper = sqlSession.getMapper(IssuesMapper.class);
            AttachmentModuleRelationMapper batchAttachmentModuleRelationMapper = sqlSession.getMapper(AttachmentModuleRelationMapper.class);

            // 批量更新
            updateIssues.forEach(issueBatchMapper::updateByPrimaryKeySelective);

            // 批量删除
            syncIssuesResult.getDeleteIssuesIds()
                    .stream()
                    .forEach(issueBatchMapper::deleteByPrimaryKey);

            try {
                // 同步附件
                syncPluginIssueAttachment(platform, syncIssuesResult, batchAttachmentModuleRelationMapper);
            } catch (Exception e) {
                LogUtil.error(e);
            }

            HashMap<String, List<CustomFieldResourceDTO>> customFieldMap = new HashMap<>();
            updateIssues.forEach(item -> {
                List<CustomFieldResourceDTO> customFieldResource = baseCustomFieldService.getCustomFieldResourceDTO(item.getCustomFields());
                customFieldMap.put(item.getId(), customFieldResource);
            });

            // 修改自定义字段
            customFieldIssuesService.batchEditFields(customFieldMap);

            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.close();
            MSException.throwException(e);
        }
    }

    private void syncPluginIssueAttachment(Platform platform, SyncIssuesResult syncIssuesResult,
                                           AttachmentModuleRelationMapper batchAttachmentModuleRelationMapper) {
        Map<String, List<PlatformAttachment>> attachmentMap = syncIssuesResult.getAttachmentMap();
        if (MapUtils.isNotEmpty(attachmentMap)) {
            for (String issueId : attachmentMap.keySet()) {
                // 查询我们平台的附件
                Set<String> jiraAttachmentSet = new HashSet<>();
                List<FileAttachmentMetadata> allMsAttachments = getIssueFileAttachmentMetadata(issueId);
                Set<String> attachmentsNameSet = allMsAttachments.stream()
                        .map(FileAttachmentMetadata::getName)
                        .collect(Collectors.toSet());

                List<PlatformAttachment> syncAttachments = attachmentMap.get(issueId);
                for (PlatformAttachment syncAttachment : syncAttachments) {
                    String fileName = syncAttachment.getFileName();
                    String fileKey = syncAttachment.getFileKey();
                    if (!attachmentsNameSet.contains(fileName)) {
                        jiraAttachmentSet.add(fileName);
                        saveAttachmentModuleRelation(platform, issueId, fileName, fileKey, batchAttachmentModuleRelationMapper);
                    }

                }

                // 删除Jira中不存在的附件
                deleteSyncAttachment(batchAttachmentModuleRelationMapper, jiraAttachmentSet, allMsAttachments);
            }
        }
    }

    private void syncAllPluginIssueAttachment(Project project, IssueSyncRequest syncIssuesResult) {
        // todo 所有平台改造完之后删除
        if (!StringUtils.equals(project.getPlatform(), IssuesManagePlatform.Jira.name())) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            AttachmentModuleRelationMapper batchAttachmentModuleRelationMapper = sqlSession.getMapper(AttachmentModuleRelationMapper.class);
            Platform platform = platformPluginService.getPlatform(project.getPlatform(), project.getWorkspaceId());
            Map<String, List<io.metersphere.xpack.track.dto.PlatformAttachment>> attachmentMap = syncIssuesResult.getAttachmentMap();
            if (MapUtils.isNotEmpty(attachmentMap)) {
                for (String issueId : attachmentMap.keySet()) {
                    // 查询我们平台的附件
                    Set<String> jiraAttachmentSet = new HashSet<>();
                    List<FileAttachmentMetadata> allMsAttachments = getIssueFileAttachmentMetadata(issueId);
                    Set<String> attachmentsNameSet = allMsAttachments.stream()
                            .map(FileAttachmentMetadata::getName)
                            .collect(Collectors.toSet());

                    List<io.metersphere.xpack.track.dto.PlatformAttachment> syncAttachments = attachmentMap.get(issueId);
                    for (io.metersphere.xpack.track.dto.PlatformAttachment syncAttachment : syncAttachments) {
                        String fileName = syncAttachment.getFileName();
                        String fileKey = syncAttachment.getFileKey();
                        if (!attachmentsNameSet.contains(fileName)) {
                            jiraAttachmentSet.add(fileName);
                            saveAttachmentModuleRelation(platform, issueId, fileName, fileKey, batchAttachmentModuleRelationMapper);
                        }

                    }

                    // 删除Jira中不存在的附件
                    deleteSyncAttachment(batchAttachmentModuleRelationMapper, jiraAttachmentSet, allMsAttachments);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void deleteSyncAttachment(AttachmentModuleRelationMapper batchAttachmentModuleRelationMapper,
                                      Set<String> jiraAttachmentSet,
                                      List<FileAttachmentMetadata> allMsAttachments) {
        // 删除Jira中不存在的附件
        if (CollectionUtils.isNotEmpty(allMsAttachments)) {
            List<FileAttachmentMetadata> deleteMsAttachments = allMsAttachments.stream()
                    .filter(msAttachment -> !jiraAttachmentSet.contains(msAttachment.getName()))
                    .collect(Collectors.toList());
            deleteMsAttachments.forEach(fileAttachmentMetadata -> {
                List<String> ids = List.of(fileAttachmentMetadata.getId());
                AttachmentModuleRelationExample example = new AttachmentModuleRelationExample();
                example.createCriteria().andAttachmentIdIn(ids).andRelationTypeEqualTo(AttachmentType.ISSUE.type());
                // 删除MS附件及关联数据
                attachmentService.deleteAttachmentByIds(ids);
                attachmentService.deleteFileAttachmentByIds(ids);
                batchAttachmentModuleRelationMapper.deleteByExample(example);
            });
        }
    }

    private void saveAttachmentModuleRelation(Platform platform, String issueId,
                                              String fileName, String fileKey,
                                              AttachmentModuleRelationMapper batchAttachmentModuleRelationMapper) {
        try {
            byte[] content = platform.getAttachmentContent(fileKey);
            if (content == null) {
                return;
            }
            FileAttachmentMetadata fileAttachmentMetadata = attachmentService
                    .saveAttachmentByBytes(content, AttachmentType.ISSUE.type(), issueId, fileName);
            AttachmentModuleRelation attachmentModuleRelation = new AttachmentModuleRelation();
            attachmentModuleRelation.setAttachmentId(fileAttachmentMetadata.getId());
            attachmentModuleRelation.setRelationId(issueId);
            attachmentModuleRelation.setRelationType(AttachmentType.ISSUE.type());
            batchAttachmentModuleRelationMapper.insert(attachmentModuleRelation);
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    private List<FileAttachmentMetadata> getIssueFileAttachmentMetadata(String issueId) {
        AttachmentRequest attachmentRequest = new AttachmentRequest();
        attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
        attachmentRequest.setBelongId(issueId);
        List<FileAttachmentMetadata> allMsAttachments = attachmentService.listMetadata(attachmentRequest);
        return allMsAttachments;
    }


    /**
     * 获取默认的自定义字段的取值，同步之后更新成第三方平台的值
     *
     * @param projectId
     * @return
     */
    public String getDefaultCustomFields(String projectId) {
        IssueTemplateDao template = trackIssueTemplateService.getTemplate(projectId);
        List<CustomFieldDao> customFields = trackCustomFieldTemplateService.getCustomFieldByTemplateId(template.getId());
        return getCustomFieldsValuesString(customFields);
    }

    public String getCustomFieldsValuesString(List<CustomFieldDao> customFields) {
        List fields = new ArrayList();
        customFields.forEach(item -> {
            Map<String, Object> field = new LinkedHashMap<>();
            field.put("customData", item.getCustomData());
            field.put("id", item.getId());
            field.put("name", item.getName());
            field.put("type", item.getType());
            String defaultValue = item.getDefaultValue();
            if (StringUtils.isNotBlank(defaultValue)) {
                field.put("value", JSON.parseObject(defaultValue));
            }
            fields.add(field);
        });
        return JSON.toJSONString(fields);
    }

    public void syncThirdPartyIssues(BiConsumer<Project, List<IssuesDao>> syncFuc, Project project, List<IssuesDao> issues) {
        try {
            syncFuc.accept(project, issues);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private String getConfig(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setWorkspaceId(orgId);
        request.setPlatform(platform);

        ServiceIntegration integration = baseIntegrationService.get(request);
        return integration.getConfiguration();
    }

    public String getLogDetails(String id) {
        IssuesWithBLOBs issuesWithBLOBs = issuesMapper.selectByPrimaryKey(id);
        if (issuesWithBLOBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(issuesWithBLOBs, TestPlanReference.issuesColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(issuesWithBLOBs.getId()), issuesWithBLOBs.getProjectId(), issuesWithBLOBs.getTitle(), issuesWithBLOBs.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(IssuesUpdateRequest issuesRequest) {
        if (issuesRequest != null) {
            issuesRequest.setCreator(SessionUtils.getUserId());
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(issuesRequest, TestPlanReference.issuesColumns);
            OperatingLogDetails details = new OperatingLogDetails(null, issuesRequest.getProjectId(), issuesRequest.getTitle(), issuesRequest.getCreator(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public List<IssuesDao> relateList(IssuesRequest request) {
        return extIssuesMapper.getIssues(request);
    }

    public void userAuth(AuthUserIssueRequest authUserIssueRequest) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setWorkspaceId(authUserIssueRequest.getWorkspaceId());
        IssuesPlatform abstractPlatform = IssueFactory.createPlatform(authUserIssueRequest.getPlatform(), issuesRequest);
        abstractPlatform.userAuth(authUserIssueRequest);
    }

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        List<PlanReportIssueDTO> planReportIssueDTOS = extIssuesMapper.selectForPlanReport(planId);
        planReportIssueDTOS = DistinctKeyUtil.distinctByKey(planReportIssueDTOS, PlanReportIssueDTO::getId);
        TestPlanFunctionResultReportDTO functionResult = report.getFunctionResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        planReportIssueDTOS.forEach(item -> {
            String status;
            // 本地缺陷
            if (StringUtils.equalsIgnoreCase(item.getPlatform(), IssuesManagePlatform.Local.name())
                    || StringUtils.isBlank(item.getPlatform())) {
                status = item.getStatus();
            } else {
                status = item.getPlatformStatus();
            }
            if (StringUtils.isBlank(status)) {
                status = IssuesStatus.NEW.toString();
            }
            TestPlanStatusCalculator.buildStatusResultMap(statusResultMap, status);
        });
        Set<String> status = statusResultMap.keySet();
        status.forEach(item -> {
            TestPlanStatusCalculator.addToReportStatusResultList(statusResultMap, statusResult, item);
        });
        functionResult.setIssueData(statusResult);
    }

    public List<IssuesDao> getIssuesByPlanId(String planId) {
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setPlanId(planId);
        List<IssuesDao> planIssues = extIssuesMapper.getPlanIssues(issueRequest);

        buildCustomField(planIssues);

        replaceStatus(planIssues, planId);
        return DistinctKeyUtil.distinctByKey(planIssues, IssuesDao::getId);
    }

    /**
     * 获取缺陷状态的自定义字段替换
     *
     * @param planIssues
     * @param planId
     */
    private void replaceStatus(List<IssuesDao> planIssues, String planId) {
        TestPlanWithBLOBs testPlan = testPlanService.get(planId);
        CustomField customField = baseCustomFieldService.getCustomFieldByName(testPlan.getProjectId(), SystemCustomField.ISSUE_STATUS);
        planIssues.forEach(issue -> {
            List<CustomFieldDao> fields = issue.getFields();
            if (CollectionUtils.isNotEmpty(fields)) {
                for (CustomFieldDao field : fields) {
                    if (field.getId().equals(customField.getId())) {
                        List<CustomFieldOptionDTO> options = JSON.parseArray(customField.getOptions(), CustomFieldOptionDTO.class);
                        for (CustomFieldOptionDTO option : options) {
                            String value = field.getValue();
                            if (StringUtils.isNotBlank(value)) {
                                value = (String) JSON.parseObject(value);
                            }
                            if (StringUtils.equals(option.getValue(), value)) {
                                if (option.getSystem()) {
                                    issue.setStatus(option.getValue());
                                } else {
                                    issue.setStatus(option.getText());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    public void changeStatus(IssuesRequest request) {
        String issuesId = request.getId();
        String status = request.getStatus();
        if (StringUtils.isBlank(issuesId) || StringUtils.isBlank(status)) {
            return;
        }
        IssuesWithBLOBs issue = issuesMapper.selectByPrimaryKey(issuesId);
        Project project = projectMapper.selectByPrimaryKey(issue.getProjectId());
        if (project == null) {
            return;
        }
        String templateId = project.getIssueTemplateId();
        if (StringUtils.isNotBlank(templateId)) {
            // 模版对于同一个系统字段应该只关联一次
            CustomField customField = baseCustomFieldService.getCustomFieldByName(issue.getProjectId(), SystemCustomField.ISSUE_STATUS);
            if (customField != null) {
                String fieldId = customField.getId();
                CustomFieldResourceDTO resource = new CustomFieldResourceDTO();
                resource.setFieldId(fieldId);
                resource.setResourceId(issue.getId());
                resource.setValue(JSON.toJSONString(status));
                customFieldIssuesService.editFields(issue.getId(), Collections.singletonList(resource));
            }
        }
    }

    public List<IssuesStatusCountDao> getCountByStatus(IssuesCountRequest request) {
        request.setCreator(SessionUtils.getUserId());
        List<IssuesStatusCountDao> countByStatus = extIssuesMapper.getCountByStatus(request);
        countByStatus.forEach(item -> {
            if (StringUtils.isBlank(item.getStatusValue())) {
                item.setStatusValue(IssuesStatus.NEW.toString());
            } else {
                item.setStatusValue(item.getStatusValue().replace("\"", StringUtils.EMPTY));
            }
        });
        return countByStatus;
    }

    public List<String> getFollows(String issueId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(issueId)) {
            return result;
        }
        IssueFollowExample example = new IssueFollowExample();
        example.createCriteria().andIssueIdEqualTo(issueId);
        List<IssueFollow> follows = issueFollowMapper.selectByExample(example);
        if (follows == null || follows.size() == 0) {
            return result;
        }
        result = follows.stream().map(IssueFollow::getFollowId).distinct().collect(Collectors.toList());
        return result;
    }

    public List<IssuesWithBLOBs> getIssuesByPlatformIds(List<String> platformIds, String projectId) {

        if (CollectionUtils.isEmpty(platformIds)) return new ArrayList<>();
        IssuesExample example = new IssuesExample();
        example.createCriteria()
                .andPlatformIdIn(platformIds)
                .andProjectIdEqualTo(projectId);
        return issuesMapper.selectByExampleWithBLOBs(example);
    }

    public IssueTemplateDao getThirdPartTemplate(String projectId) {
        IssueTemplateDao issueTemplateDao = new IssueTemplateDao();
        if (StringUtils.isNotBlank(projectId)) {
            Project project = baseProjectService.getProjectById(projectId);
            List<PlatformCustomFieldItemDTO> thirdPartCustomField = platformPluginService.getPlatform(project.getPlatform(), project.getWorkspaceId())
                    .getThirdPartCustomField(PlatformPluginService.getCompatibleProjectConfig(project));
            List<CustomFieldDao> customFieldDaoList = JSON.parseArray(JSON.toJSONString(thirdPartCustomField), CustomFieldDao.class);
            issueTemplateDao.setCustomFields(customFieldDaoList);
            issueTemplateDao.setPlatform(project.getPlatform());
        }
        return issueTemplateDao;
    }

    public IssuesRequest getDefaultIssueRequest(String projectId, String workspaceId) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setProjectId(projectId);
        issuesRequest.setWorkspaceId(workspaceId);
        return issuesRequest;
    }

    public List getDemandList(String projectId) {
        Project project = baseProjectService.getProjectById(projectId);
        String workspaceId = project.getWorkspaceId();

        if (PlatformPluginService.isPluginPlatform(project.getPlatform())) {
            return platformPluginService.getPlatform(project.getPlatform())
                    .getDemands(PlatformPluginService.getCompatibleProjectConfig(project));
        } else {
            IssuesRequest issueRequest = new IssuesRequest();
            issueRequest.setWorkspaceId(workspaceId);
            issueRequest.setProjectId(projectId);
            IssuesPlatform platform = IssueFactory.createPlatform(project.getPlatform(), issueRequest);
            return platform.getDemandList(projectId);
        }
    }

    public List<IssuesDao> listByWorkspaceId(IssuesRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrderByField(request.getOrders(), "create_time"));
        return extIssuesMapper.getIssues(request);
    }

    public List<PlatformStatusDTO> getPlatformTransitions(PlatformIssueTypeRequest request) {
        List<PlatformStatusDTO> platformStatusDTOS = new ArrayList<>();

        if (!StringUtils.isBlank(request.getPlatformKey())) {
            Project project = baseProjectService.getProjectById(request.getProjectId());
            String platform = project.getPlatform();
            if (PlatformPluginService.isPluginPlatform(platform)) {
                return platformPluginService.getPlatform(platform)
                        .getStatusList(request.getPlatformKey())
                        .stream().map(item -> {
                            PlatformStatusDTO platformStatusDTO = new PlatformStatusDTO();
                            platformStatusDTO.setLabel(item.getLabel());
                            platformStatusDTO.setValue(item.getValue());
                            return platformStatusDTO;
                        })
                        .collect(Collectors.toList());
            } else {
                List<String> platforms = getPlatforms(project);
                if (CollectionUtils.isEmpty(platforms)) {
                    return platformStatusDTOS;
                }

                IssuesRequest issuesRequest = getDefaultIssueRequest(request.getProjectId(), request.getWorkspaceId());
                return IssueFactory.createPlatform(platform, issuesRequest).getTransitions(request.getPlatformKey());
            }
        }
        return platformStatusDTOS;
    }

    public boolean isThirdPartTemplate(Project project) {
        return project.getThirdPartTemplate() != null
                && project.getThirdPartTemplate()
                && PlatformPluginService.isPluginPlatform(project.getPlatform());
    }

    public void checkThirdProjectExist(Project project) {
        IssuesRequest issuesRequest = new IssuesRequest();
        if (StringUtils.isBlank(project.getId())) {
            MSException.throwException("project ID cannot be empty");
        }
        issuesRequest.setProjectId(project.getId());
        issuesRequest.setWorkspaceId(project.getWorkspaceId());
        if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Tapd.name())) {
            TapdPlatform tapd = new TapdPlatform(issuesRequest);
            this.doCheckThirdProjectExist(tapd, project.getTapdId());
        } else if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Zentao.name())) {
            ZentaoPlatform zentao = new ZentaoPlatform(issuesRequest);
            this.doCheckThirdProjectExist(zentao, project.getZentaoId());
        }
    }

    public void issueImportTemplate(String projectId, HttpServletResponse response) {
        Map<String, String> userMap = baseUserService.getProjectMemberOption(projectId).stream().collect(Collectors.toMap(User::getId, User::getName));
        // 获取缺陷模板及自定义字段
        IssueTemplateDao issueTemplate = getIssueTemplateByProjectId(projectId);
        List<CustomFieldDao> customFields = Optional.ofNullable(issueTemplate.getCustomFields()).orElse(new ArrayList<>());
        // 根据自定义字段获取表头
        List<List<String>> heads = new IssueExcelDataFactory().getIssueExcelDataLocal().getHead(issueTemplate.getIsThirdTemplate(), customFields, null);
        // 导出空模板, heads->表头, headHandler->表头处理
        IssueTemplateHeadWriteHandler headHandler = new IssueTemplateHeadWriteHandler(userMap, heads, issueTemplate.getCustomFields());
        new EasyExcelExporter(new IssueExcelDataFactory().getExcelDataByLocal())
                .exportByCustomWriteHandler(response, heads, null, Translator.get("issue_import_template_name"),
                        Translator.get("issue_import_template_sheet"), headHandler);
    }

    public ExcelResponse issueImport(IssueImportRequest request, MultipartFile importFile) {
        if (importFile == null) {
            MSException.throwException(Translator.get("upload_fail"));
        }
        Map<String, String> userMap = baseUserService.getProjectMemberOption(request.getProjectId()).stream().collect(Collectors.toMap(User::getId, User::getName));
        // 获取缺陷模板及自定义字段
        IssueTemplateDao issueTemplate = getIssueTemplateByProjectId(request.getProjectId());
        List<CustomFieldDao> customFields = Optional.ofNullable(issueTemplate.getCustomFields()).orElse(new ArrayList<>());
        // 获取本地EXCEL数据对象
        Class clazz = new IssueExcelDataFactory().getExcelDataByLocal();
        // IssueExcelListener读取file内容
        IssueExcelListener issueExcelListener = new IssueExcelListener(request, clazz, issueTemplate.getIsThirdTemplate(), customFields, userMap);
        try {
            EasyExcelFactory.read(importFile.getInputStream(), issueExcelListener).sheet().doRead();
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            e.printStackTrace();
        }
        // 获取错误信息并返回
        List<ExcelErrData<IssueExcelData>> errList = issueExcelListener.getErrList();
        ExcelResponse excelResponse = new ExcelResponse();
        if (CollectionUtils.isNotEmpty(errList)) {
            excelResponse.setErrList(errList);
            excelResponse.setSuccess(Boolean.FALSE);
        } else {
            excelResponse.setSuccess(Boolean.TRUE);
        }
        return excelResponse;
    }

    public void issueExport(IssueExportRequest request, HttpServletResponse response) {
        EasyExcelExporter.resetCellMaxTextLength();
        Map<String, String> userMap = baseUserService.getProjectMemberOption(request.getProjectId()).stream().collect(Collectors.toMap(User::getId, User::getName));
        // 获取缺陷模板及自定义字段
        IssueTemplateDao issueTemplate = getIssueTemplateByProjectId(request.getProjectId());
        List<CustomFieldDao> customFields = Optional.ofNullable(issueTemplate.getCustomFields()).orElse(new ArrayList<>());
        // 根据自定义字段获取表头内容
        List<List<String>> heads = new IssueExcelDataFactory().getIssueExcelDataLocal().getHead(issueTemplate.getIsThirdTemplate(), customFields, request);
        // 获取导出缺陷列表
        List<IssuesDao> exportIssues = getExportIssues(request, issueTemplate.getIsThirdTemplate(), customFields);
        // 解析issue对象数据->excel对象数据
        List<IssueExcelData> excelDataList = parseIssueDataToExcelData(exportIssues);
        // 解析excel对象数据->excel列表数据
        List<List<Object>> data = parseExcelDataToList(heads, excelDataList);
        // 导出EXCEL
        IssueTemplateHeadWriteHandler headHandler = new IssueTemplateHeadWriteHandler(userMap, heads, issueTemplate.getCustomFields());
        // heads-> 表头内容, data -> 导出EXCEL列表数据, headHandler -> 表头处理
        new EasyExcelExporter(new IssueExcelDataFactory().getExcelDataByLocal())
                .exportByCustomWriteHandler(response, heads, data, Translator.get("issue_list_export_excel"),
                        Translator.get("issue_list_export_excel_sheet"), headHandler);
    }

    public List<IssuesDao> getExportIssues(IssueExportRequest exportRequest, Boolean isThirdTemplate, List<CustomFieldDao> customFields) {
        // 根据列表条件获取符合缺陷集合
        IssuesRequest request = new IssuesRequest();
        request.setProjectId(exportRequest.getProjectId());
        request.setWorkspaceId(exportRequest.getWorkspaceId());
        request.setSelectAll(exportRequest.getIsSelectAll());
        request.setExportIds(exportRequest.getExportIds());
        // 列表排序
        request.setOrders(exportRequest.getOrders());
        request.setOrders(ServiceUtils.getDefaultOrderByField(request.getOrders(), "create_time"));
        request.getOrders().forEach(order -> {
            if (StringUtils.isNotEmpty(order.getName()) && order.getName().startsWith("custom")) {
                request.setIsCustomSorted(true);
                request.setCustomFieldId(order.getName().replace("custom_", StringUtils.EMPTY));
                order.setPrefix("cfi");
                order.setName("value");
            }
        });
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<IssuesDao> issues = extIssuesMapper.getIssues(request);

        Map<String, Set<String>> caseSetMap = getCaseSetMap(issues);
        Map<String, User> userMap = getUserMap(issues);
        Map<String, String> planMap = getPlanMap(issues);
        Map<String, List<IssueCommentDTO>> commentMap = getCommentMap(issues);

        // 设置creator, caseCount, commnet
        issues.forEach(item -> {
            User createUser = userMap.get(item.getCreator());
            if (createUser != null) {
                item.setCreatorName(createUser.getName());
            }
            String resourceName = planMap.get(item.getResourceId());
            if (StringUtils.isNotBlank(resourceName)) {
                item.setResourceName(resourceName);
            }

            Set<String> caseIdSet = caseSetMap.get(item.getId());
            if (caseIdSet == null) {
                caseIdSet = new HashSet<>();
            }
            item.setCaseIds(new ArrayList<>(caseIdSet));
            item.setCaseCount(caseIdSet.size());
            List<IssueCommentDTO> commentDTOList = commentMap.get(item.getId());
            if (CollectionUtils.isNotEmpty(commentDTOList) && commentDTOList.size() > 0) {
                List<String> comments = commentDTOList.stream().map(IssueCommentDTO::getDescription).collect(Collectors.toList());
                item.setComment(StringUtils.join(comments, ";"));
            }
        });
        // 解析自定义字段
        buildCustomField(issues, isThirdTemplate, customFields);
        return issues;
    }

    private List<IssueExcelData> parseIssueDataToExcelData(List<IssuesDao> exportIssues) {
        List<IssueExcelData> excelDataList = new ArrayList<>();
        for (int i = 0; i < exportIssues.size(); i++) {
            IssuesDao issuesDao = exportIssues.get(i);
            IssueExcelData excelData = new IssueExcelData();
            BeanUtils.copyBean(excelData, issuesDao);
            buildCustomData(issuesDao, excelData);
            excelDataList.add(excelData);
        }
        return excelDataList;
    }

    private void buildCustomData(IssuesDao issuesDao, IssueExcelData excelData) {
        if (CollectionUtils.isNotEmpty(issuesDao.getFields())) {
            Map<String, Object> customData = new LinkedHashMap<>();
            issuesDao.getFields().forEach(field -> {
                customData.put(field.getName(), field.getValue());
            });
            excelData.setCustomData(customData);
        }
    }

    private List<List<Object>> parseExcelDataToList(List<List<String>> heads, List<IssueExcelData> excelDataList) {
        List<List<Object>> result = new ArrayList<>();
        IssueExportHeadField[] exportHeadFields = IssueExportHeadField.values();
        //转化excel头
        List<String> headList = new ArrayList<>();
        for (List<String> list : heads) {
            for (String head : list) {
                headList.add(head);
            }
        }

        for (IssueExcelData data : excelDataList) {
            List<Object> rowData = new ArrayList<>();
            Map<String, Object> customData = data.getCustomData();
            for (String head : headList) {
                boolean isSystemField = false;
                for (IssueExportHeadField exportHeadField : exportHeadFields) {
                    if (StringUtils.equals(head, exportHeadField.getName())) {
                        rowData.add(exportHeadField.parseExcelDataValue(data));
                        isSystemField = true;
                        break;
                    }
                }
                if (!isSystemField) {
                    // 自定义字段
                    Object value = customData.get(head);
                    if (value == null || StringUtils.equals(value.toString(), "null")) {
                        value = StringUtils.EMPTY;
                    }
                    rowData.add(parseCustomFieldValue(value.toString()));
                }
            }
            result.add(rowData);
        }
        return result;
    }

    private IssueTemplateDao getIssueTemplateByProjectId(String projectId) {
        IssueTemplateDao issueTemplateDao;
        Project project = baseProjectService.getProjectById(projectId);
        if (PlatformPluginService.isPluginPlatform(project.getPlatform())
                && project.getThirdPartTemplate()) {
            // 第三方Jira平台
            issueTemplateDao = getThirdPartTemplate(project.getId());
            issueTemplateDao.setIsThirdTemplate(Boolean.TRUE);
        } else {
            issueTemplateDao = trackIssueTemplateService.getTemplate(projectId);
            issueTemplateDao.setIsThirdTemplate(Boolean.FALSE);
        }
        return issueTemplateDao;
    }

    private void doCheckThirdProjectExist(AbstractIssuePlatform platform, String relateId) {
        if (StringUtils.isBlank(relateId)) {
            MSException.throwException(Translator.get("issue_project_not_exist"));
        }
        Boolean exist = platform.checkProjectExist(relateId);
        if (BooleanUtils.isFalse(exist)) {
            MSException.throwException(Translator.get("issue_project_not_exist"));
        }
    }

    private List<IssuesDao> filterSyncIssuesByCreated(List<IssuesDao> issues, IssueSyncRequest syncRequest) {
        List<IssuesDao> filterIssues = issues.stream().filter(issue -> {
            if (syncRequest.isPre()) {
                return issue.getCreateTime() <= syncRequest.getCreateTime();
            } else {
                return issue.getCreateTime() >= syncRequest.getCreateTime();
            }
        }).collect(Collectors.toList());
        return filterIssues;
    }

    private void uploadAzureCopyAttachment(AttachmentRequest attachmentRequest, String platform, String platformId) {
        List<String> attachmentIds = attachmentService.getAttachmentIdsByParam(attachmentRequest);
        if (CollectionUtils.isNotEmpty(attachmentIds)) {
            attachmentIds.forEach(attachmentId -> {
                FileAttachmentMetadata fileAttachmentMetadata = attachmentService.getFileAttachmentMetadataByFileId(attachmentId);
                File file = new File(fileAttachmentMetadata.getFilePath() + "/" + fileAttachmentMetadata.getName());
                IssuesRequest createRequest = new IssuesRequest();
                createRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                createRequest.setProjectId(SessionUtils.getCurrentProjectId());
                IssuesPlatform azurePlatform = Objects.requireNonNull(IssueFactory.createPlatform(platform, createRequest));
                IssuesUpdateRequest uploadRequest = new IssuesUpdateRequest();
                uploadRequest.setPlatformId(platformId);
                azurePlatform.syncIssuesAttachment(uploadRequest, file, AttachmentSyncType.UPLOAD);
            });
        }
    }

    private String parseCustomFieldValue(String value) {
        if (value.contains(",")) {
            value = value.replaceAll(",", ";");
        }
        if (value.contains("\"")) {
            value = value.replaceAll("\"", StringUtils.EMPTY);
        }
        if (value.contains("[") || value.contains("]")) {
            value = value.replaceAll("]", StringUtils.EMPTY).replaceAll("\\[", StringUtils.EMPTY);
        }
        return value;
    }

    private String parseOptionValue(String options, String tarVal) {
        if (StringUtils.isEmpty(options) || StringUtils.isEmpty(tarVal)) {
            return StringUtils.EMPTY;
        }
        List<Map> optionList = JSON.parseArray(options, Map.class);
        for (Map option : optionList) {
            String text = option.get("text").toString();
            String value = option.get("value").toString();
            if (StringUtils.containsIgnoreCase(tarVal, value)) {
                tarVal = tarVal.replaceAll(value, text);
            }
        }
        return tarVal;
    }

    public Issues checkIssueExist(Integer num, String projectId) {
        IssuesExample example = new IssuesExample();
        example.createCriteria().andNumEqualTo(num).andProjectIdEqualTo(projectId);
        List<Issues> issues = issuesMapper.selectByExample(example);
        return CollectionUtils.isNotEmpty(issues) && issues.size() > 0 ? issues.get(0) : null;
    }

    public void saveImportData(List<IssuesUpdateRequest> issues) {
        issues.parallelStream().forEach(issue -> {
            addIssues(issue, null);
        });
    }

    public void updateImportData(List<IssuesUpdateRequest> issues) {
        issues.parallelStream().forEach(issue -> {
            updateIssues(issue);
        });
    }

    public void setFilterIds(IssuesRequest request) {
        List<String> issueIds = new ArrayList<>();
        if (request.getThisWeekUnClosedTestPlanIssue()) {
            issueIds = extIssuesMapper.getTestPlanThisWeekIssue(request.getProjectId());
        } else if (request.getAllTestPlanIssue() || request.getUnClosedTestPlanIssue()) {
            issueIds = extIssuesMapper.getTestPlanIssue(request.getProjectId());
        } else {
            issueIds = Collections.EMPTY_LIST;
        }

        Map<String, String> statusMap = customFieldIssuesService.getIssueStatusMap(issueIds, request.getProjectId());
        if (MapUtils.isEmpty(statusMap) && CollectionUtils.isNotEmpty(issueIds)) {
            // 未找到自定义字段状态, 则获取平台状态
            IssuesRequest issuesRequest = new IssuesRequest();
            issuesRequest.setProjectId(SessionUtils.getCurrentProjectId());
            issuesRequest.setFilterIds(issueIds);
            List<IssuesDao> issues = extIssuesMapper.getIssues(issuesRequest);
            statusMap = issues.stream().collect(Collectors.toMap(IssuesDao::getId, i -> Optional.ofNullable(i.getPlatformStatus()).orElse("new")));
        }

        if (MapUtils.isEmpty(statusMap)) {
            request.setFilterIds(issueIds);
        } else {
            if (request.getThisWeekUnClosedTestPlanIssue() || request.getUnClosedTestPlanIssue()) {
                CustomField customField = baseCustomFieldService.getCustomFieldByName(SessionUtils.getCurrentProjectId(), SystemCustomField.ISSUE_STATUS);
                JSONArray statusArray = JSONArray.parseArray(customField.getOptions());
                Map<String, String> tmpStatusMap = statusMap;
                List<String> unClosedIds = issueIds.stream()
                        .filter(id -> !StringUtils.equals(tmpStatusMap.getOrDefault(id, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY), "closed"))
                        .collect(Collectors.toList());
                Iterator<String> iterator = unClosedIds.iterator();
                while (iterator.hasNext()) {
                    String unClosedId = iterator.next();
                    String status = statusMap.getOrDefault(unClosedId, StringUtils.EMPTY).replaceAll("\"", StringUtils.EMPTY);
                    IssueStatus statusEnum = IssueStatus.getEnumByName(status);
                    if (statusEnum == null) {
                        boolean exist = false;
                        for (int i = 0; i < statusArray.size(); i++) {
                            JSONObject statusObj = (JSONObject) statusArray.get(i);
                            if (StringUtils.equals(status, statusObj.get("value").toString())) {
                                exist = true;
                            }
                        }
                        if (!exist) {
                            iterator.remove();
                        }
                    }
                }
                request.setFilterIds(unClosedIds);
            } else {
                request.setFilterIds(issueIds);
            }
        }
    }

    public boolean thirdPartTemplateEnable(String projectId) {
        Project project = baseProjectService.getProjectById(projectId);
        return BooleanUtils.isTrue(project.getThirdPartTemplate())
                && platformPluginService.isThirdPartTemplateSupport(project.getPlatform());
    }

    public boolean syncThirdPartyAllIssues(IssueSyncRequest syncRequest) {
        syncRequest.setProjectId(syncRequest.getProjectId());
        XpackIssueService xpackIssueService = CommonBeanFactory.getBean(XpackIssueService.class);
        if (StringUtils.isNotBlank(syncRequest.getProjectId())) {
            // 获取当前项目执行同步缺陷Key
            String syncValue = getSyncKey(syncRequest.getProjectId());
            // 存在即正在同步中
            if (StringUtils.isNotEmpty(syncValue)) {
                return false;
            }
            // 不存在则设置Key, 设置过期时间, 执行完成后delete掉
            setSyncKey(syncRequest.getProjectId());

            try {
                Project project = baseProjectService.getProjectById(syncRequest.getProjectId());

                if (!isThirdPartTemplate(project)) {
                    syncRequest.setDefaultCustomFields(getDefaultCustomFields(syncRequest.getProjectId()));
                }

                xpackIssueService.syncThirdPartyIssues(project, syncRequest);

                syncAllPluginIssueAttachment(project, syncRequest);
            } catch (Exception e) {
                LogUtil.error(e);
                MSException.throwException(e);
            } finally {
                deleteSyncKey(syncRequest.getProjectId());
            }
        }
        return true;
    }
}
