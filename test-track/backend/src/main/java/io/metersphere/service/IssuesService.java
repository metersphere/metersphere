package io.metersphere.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.constants.FileAssociationType;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.IssuesStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.plan.service.TestPlanTestCaseService;
import io.metersphere.xpack.track.dto.AttachmentSyncType;
import io.metersphere.constants.AttachmentType;
import io.metersphere.constants.SystemCustomField;
import io.metersphere.dto.CustomFieldDao;
import io.metersphere.xpack.track.dto.IssueTemplateDao;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestPlanReference;
import io.metersphere.dto.*;
import io.metersphere.plan.dto.PlanReportIssueDTO;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.request.IntegrationRequest;
import io.metersphere.request.issues.JiraIssueTypeRequest;
import io.metersphere.request.issues.PlatformIssueTypeRequest;
import io.metersphere.request.testcase.AuthUserIssueRequest;
import io.metersphere.request.testcase.IssuesCountRequest;
import io.metersphere.xpack.track.dto.PlatformUser;
import io.metersphere.service.issue.domain.jira.JiraIssueType;
import io.metersphere.service.issue.domain.zentao.ZentaoBuild;
import io.metersphere.request.attachment.AttachmentRequest;
import io.metersphere.xpack.track.dto.DemandDTO;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import io.metersphere.service.issue.platform.*;
import io.metersphere.service.remote.project.TrackCustomFieldTemplateService;
import io.metersphere.service.remote.project.TrackIssueTemplateService;
import io.metersphere.service.wapper.TrackProjectService;
import io.metersphere.xpack.track.dto.PlatformStatusDTO;
import io.metersphere.xpack.track.issue.IssuesPlatform;
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
import java.io.File;
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
    IssueFileMapper issueFileMapper;
    @Resource
    private AttachmentService attachmentService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileMetadataMapper fileMetadataMapper;

    private static final String SYNC_THIRD_PARTY_ISSUES_KEY = "ISSUE:SYNC";

    public void testAuth(String workspaceId, String platform) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setWorkspaceId(workspaceId);
        IssuesPlatform abstractPlatform = IssueFactory.createPlatform(platform, issuesRequest);
        abstractPlatform.testAuth();
    }


    public IssuesWithBLOBs addIssues(IssuesUpdateRequest issuesRequest, List<MultipartFile> files) {
        List<IssuesPlatform> platformList = getAddPlatforms(issuesRequest);
        IssuesWithBLOBs issues = null;
        for (IssuesPlatform platform : platformList) {
            issues = platform.addIssue(issuesRequest);
        }
        if (issuesRequest.getIsPlanEdit()) {
            issuesRequest.getAddResourceIds().forEach(l -> {
                testCaseIssueService.updateIssuesCount(l);
            });
        }
        saveFollows(issuesRequest.getId(), issuesRequest.getFollows());
        customFieldIssuesService.addFields(issuesRequest.getId(), issuesRequest.getAddFields());
        customFieldIssuesService.editFields(issuesRequest.getId(), issuesRequest.getEditFields());
        if (StringUtils.isNotEmpty(issuesRequest.getCopyIssueId())) {
            // 复制新增, 同步缺陷的MS附件
            AttachmentRequest attachmentRequest = new AttachmentRequest();
            attachmentRequest.setCopyBelongId(issuesRequest.getCopyIssueId());
            attachmentRequest.setBelongId(issues.getId());
            attachmentRequest.setBelongType(AttachmentType.ISSUE.type());
            attachmentService.copyAttachment(attachmentRequest);
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
                    fileAttachmentMetadata.setCreator(fileMetadata.getCreateUser() == null ? "" : fileMetadata.getCreateUser());
                    fileAttachmentMetadata.setFilePath(fileMetadata.getPath() == null ? "" : fileMetadata.getPath());
                    fileAttachmentMetadataBatchMapper.insert(fileAttachmentMetadata);
                    // 下载文件管理文件, 同步到第三方平台
                    File refFile = attachmentService.downloadMetadataFile(filemetaId, fileMetadata.getName());
                    IssuesRequest addIssueRequest = new IssuesRequest();
                    addIssueRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
                    Objects.requireNonNull(IssueFactory.createPlatform(platform, addIssueRequest))
                            .syncIssuesAttachment(issuesRequest, refFile, AttachmentSyncType.UPLOAD);
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

    public IssuesWithBLOBs updateIssues(IssuesUpdateRequest issuesRequest) {
        List<IssuesPlatform> platformList = getUpdatePlatforms(issuesRequest);
        platformList.forEach(platform -> {
            platform.updateIssue(issuesRequest);
        });
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
        return disconnectIssue(issues);
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
            issue.setStatus(statusMap.getOrDefault(issue.getId(), "").replaceAll("\"", ""));
        }
    }

    public IssuesWithBLOBs getIssue(String id) {
        IssuesDao issuesWithBLOBs = extIssuesMapper.selectByPrimaryKey(id);
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
            if (StringUtils.isNotBlank(jiraKey) && StringUtils.equals(project.getPlatform(), IssuesManagePlatform.Jira.toString())) {
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
        IssuesPlatform platform = IssueFactory.createPlatform(issuesWithBLOBs.getPlatform(), issuesRequest);
        platform.deleteIssue(id);
        // 删除缺陷对应的附件
        AttachmentRequest request = new AttachmentRequest();
        request.setBelongId(id);
        request.setBelongType(AttachmentType.ISSUE.type());
        attachmentService.deleteAttachment(request);
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
                request.setCustomFieldId(order.getName().substring(order.getName().indexOf("-") + 1));
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
        return url.replaceAll("platform=Jira&",
                "platform=Jira&project_id=" + projectId + "&workspace_id=" + workspaceId + "&");
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

    public Map<String, IssuesPlatform> getPlatformMap(IssuesRequest request) {
        Project project = baseProjectService.getProjectById(request.getProjectId());
        List<String> platforms = getPlatforms(project);
        platforms.add(IssuesManagePlatform.Local.toString());
        return IssueFactory.createPlatformsForMap(platforms, request);
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
                IssuesPlatform platform = IssueFactory.createPlatform(project.getPlatform(), issuesRequest);
                syncThirdPartyIssues(platform::syncIssues, project, issues);
            } catch (Exception e) {
                throw e;
            } finally {
                deleteSyncKey(projectId);
            }
        }
        return true;
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

        return disconnectIssue(planIssues);
    }

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
                            if (value != null) {
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

    public List<IssuesDao> disconnectIssue(List<IssuesDao> issues) {
        Set<String> ids = new HashSet<>(issues.size());
        Iterator<IssuesDao> iterator = issues.iterator();
        while (iterator.hasNext()) {
            IssuesDao next = iterator.next();
            if (ids.contains(next.getId())) {
                iterator.remove();
            }
            ids.add(next.getId());
        }
        return issues;
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
            CustomField customField = baseCustomFieldService.getCustomFieldByName(templateId, SystemCustomField.ISSUE_STATUS);
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
                item.setStatusValue(item.getStatusValue().replace("\"", ""));
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

    public List<IssuesDao> getPlatformIssueByIds(List<String> platformIds, String projectId) {
        // todo 是否保留
        List<IssuesDao> issues = extIssuesMapper.getPlatformIssueByIds(platformIds, projectId);
        if (CollectionUtils.isEmpty(issues)) {
            return issues;
        }
        List<String> issueIds = issues.stream().map(IssuesDao::getId).collect(Collectors.toList());
        List<IssuesDao> issuesList = extIssuesMapper.getIssueCustomFields(issueIds);
        Map<String, List<CustomFieldItemDTO>> map = new HashMap<>();
        issuesList.forEach(f -> {
            CustomFieldItemDTO dto = new CustomFieldItemDTO();
            dto.setId(f.getFieldId());
            dto.setName(f.getFieldName());
            dto.setType(f.getFieldType());
            dto.setValue(f.getFieldValue());
            dto.setCustomData(f.getCustomData());
            List<CustomFieldItemDTO> list = Optional.ofNullable(map.get(f.getId())).orElse(new ArrayList<>());
            map.put(f.getId(), list);
            list.add(dto);
        });
        issues.forEach(i -> i.setCustomFieldList(map.getOrDefault(i.getId(), new ArrayList<>())));
        return issues;
    }


    public IssueTemplateDao getThirdPartTemplate(String projectId) {
        if (StringUtils.isNotBlank(projectId)) {
            Project project = baseProjectService.getProjectById(projectId);
            return IssueFactory.createPlatform(IssuesManagePlatform.Jira.toString(), getDefaultIssueRequest(projectId, project.getWorkspaceId()))
                    .getThirdPartTemplate();
        }
        return new IssueTemplateDao();
    }

    public IssuesRequest getDefaultIssueRequest(String projectId, String workspaceId) {
        IssuesRequest issuesRequest = new IssuesRequest();
        issuesRequest.setProjectId(projectId);
        issuesRequest.setWorkspaceId(workspaceId);
        return issuesRequest;
    }

    public List<JiraIssueType> getIssueTypes(JiraIssueTypeRequest request) {
        IssuesRequest issuesRequest = getDefaultIssueRequest(request.getProjectId(), request.getWorkspaceId());
        JiraPlatform platform = (JiraPlatform) IssueFactory.createPlatform(IssuesManagePlatform.Jira.toString(), issuesRequest);
        if (StringUtils.isNotBlank(request.getJiraKey())) {
            return platform.getIssueTypes(request.getJiraKey());
        } else {
            return new ArrayList<>();
        }
    }

    public List<DemandDTO> getDemandList(String projectId) {
        Project project = baseProjectService.getProjectById(projectId);
        String workspaceId = project.getWorkspaceId();
        IssuesRequest issueRequest = new IssuesRequest();
        issueRequest.setWorkspaceId(workspaceId);
        issueRequest.setProjectId(projectId);
        IssuesPlatform platform = IssueFactory.createPlatform(project.getPlatform(), issueRequest);
        return platform.getDemandList(projectId);
    }

    public List<IssuesDao> listByWorkspaceId(IssuesRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrderByField(request.getOrders(), "create_time"));
        return extIssuesMapper.getIssues(request);
    }

    public List<PlatformStatusDTO> getPlatformTransitions(PlatformIssueTypeRequest request) {
        List<PlatformStatusDTO> platformStatusDTOS = new ArrayList<>();

        if (!StringUtils.isBlank(request.getPlatformKey())) {
            Project project = baseProjectService.getProjectById(request.getProjectId());
            List<String> platforms = getPlatforms(project);
            if (CollectionUtils.isEmpty(platforms)) {
                return platformStatusDTOS;
            }

            IssuesRequest issuesRequest = getDefaultIssueRequest(request.getProjectId(), request.getWorkspaceId());
            Map<String, IssuesPlatform> platformMap = IssueFactory.createPlatformsForMap(platforms, issuesRequest);
            try {
                if (platformMap.size() > 1) {
                    MSException.throwException(Translator.get("project_reference_multiple_plateform"));
                }
                Optional<IssuesPlatform> platformOptional = platformMap.values().stream().findFirst();
                if (platformOptional.isPresent()) {
                    platformStatusDTOS = platformOptional.get().getTransitions(request.getPlatformKey());
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }

        return platformStatusDTOS;
    }

    public void deleteIssueAttachments(String issueId) {
        attachmentService.deleteAttachment(AttachmentType.ISSUE.type(), issueId);
        IssueFileExample example = new IssueFileExample();
        example.createCriteria().andIssueIdEqualTo(issueId);
        List<IssueFile> issueFiles = issueFileMapper.selectByExample(example);
        if (issueFiles.size() == 0) {
            return;
        }
        List<String> ids = issueFiles.stream().map(IssueFile::getFileId).collect(Collectors.toList());
        attachmentService.deleteFileAttachmentByIds(ids);
        issueFileMapper.deleteByExample(example);
    }

    public boolean isThirdPartTemplate(Project project) {
        return project.getThirdPartTemplate() != null && project.getThirdPartTemplate() && project.getPlatform().equals(IssuesManagePlatform.Jira.name());
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
        } else if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Jira.name())) {
            JiraPlatform jira = new JiraPlatform(issuesRequest);
            this.doCheckThirdProjectExist(jira, project.getJiraKey());
        } else if (StringUtils.equalsIgnoreCase(project.getPlatform(), IssuesManagePlatform.Zentao.name())) {
            ZentaoPlatform zentao = new ZentaoPlatform(issuesRequest);
            this.doCheckThirdProjectExist(zentao, project.getZentaoId());
        }
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
}
