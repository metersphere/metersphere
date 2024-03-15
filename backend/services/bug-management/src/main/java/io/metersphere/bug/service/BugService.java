package io.metersphere.bug.service;

import io.metersphere.bug.constants.BugExportColumns;
import io.metersphere.bug.domain.*;
import io.metersphere.bug.dto.BugExportHeaderModel;
import io.metersphere.bug.dto.BugTemplateInjectField;
import io.metersphere.bug.dto.request.*;
import io.metersphere.bug.dto.response.*;
import io.metersphere.bug.enums.BugAttachmentSourceType;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.enums.BugTemplateCustomField;
import io.metersphere.bug.mapper.*;
import io.metersphere.bug.utils.ExportUtils;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.dto.SyncBugResult;
import io.metersphere.plugin.platform.dto.reponse.PlatformBugDTO;
import io.metersphere.plugin.platform.dto.reponse.PlatformBugUpdateDTO;
import io.metersphere.plugin.platform.dto.reponse.PlatformCustomFieldItemDTO;
import io.metersphere.plugin.platform.dto.request.*;
import io.metersphere.plugin.platform.enums.SyncAttachmentType;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.mapper.FileAssociationMapper;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.*;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateCustomField;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.TemplateCustomFieldDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.BaseTemplateCustomFieldService;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.UserPlatformAccountService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.metersphere.bug.enums.result.BugResultCode.BUG_NOT_EXIST;
import static io.metersphere.bug.enums.result.BugResultCode.NOT_LOCAL_BUG_ERROR;

/**
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BugService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private ExtBugMapper extBugMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    protected TemplateMapper templateMapper;
    @Resource
    private BugContentMapper bugContentMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private UserPlatformAccountService userPlatformAccountService;
    @Resource
    private BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    private BugCommonService bugCommonService;
    @Resource
    private BugCustomFieldMapper bugCustomFieldMapper;
    @Resource
    private ExtBugCustomFieldMapper extBugCustomFieldMapper;
    @Resource
    private ExtBugRelateCaseMapper extBugRelateCaseMapper;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private BugLocalAttachmentMapper bugLocalAttachmentMapper;
    @Resource
    private BugRelationCaseMapper bugRelationCaseMapper;
    @Resource
    private FileAssociationMapper fileAssociationMapper;
    @Resource
    private ExtBugLocalAttachmentMapper extBugLocalAttachmentMapper;
    @Resource
    private FileService fileService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BugFollowerMapper bugFollowerMapper;
    @Resource
    private BugExportService bugExportService;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private BugSyncExtraService bugSyncExtraService;
    @Resource
    private BugSyncNoticeService bugSyncNoticeService;
    @Resource
    private BugStatusService bugStatusService;
    @Resource
    private BugAttachmentService bugAttachmentService;
    @Resource
    private BugPlatformService bugPlatformService;

    public static final Long INTERVAL_POS = 5000L;

    private static final int MAX_TAG_SIZE = 10;

    /**
     * 缺陷列表查询
     *
     * @param request 列表请求参数
     * @return 缺陷列表
     */
    public List<BugDTO> list(BugPageRequest request) {
        List<BugDTO> bugs = extBugMapper.list(request, null);
        if (CollectionUtils.isEmpty(bugs)) {
            return new ArrayList<>();
        }
        // 处理自定义字段
        List<BugDTO> bugList = handleCustomField(bugs, request.getProjectId());
        return buildExtraInfo(bugList);
    }

    /**
     * 创建或编辑缺陷
     *
     * @param request 缺陷请求参数
     * @param files 附件集合
     * @param currentUser 当前用户
     * @param currentOrgId 当前组织ID
     * @param isUpdate 是否更新
     * @return 缺陷
     */
    public Bug addOrUpdate(BugEditRequest request, List<MultipartFile> files, String currentUser, String currentOrgId, boolean isUpdate) {
        // 校验标签长度
        this.checkTagLength(request.getTags());
        /*
         *  缺陷创建或者修改逻辑:
         *  1. 判断所属项目是否关联第三方平台;
         *  2. 第三方平台缺陷需调用插件同步缺陷至其他平台(自定义字段需处理);
         *  3. 保存MS缺陷(基础字段, 自定义字段)
         *  4. 处理附件(第三方平台缺陷需异步调用接口同步附件至第三方)
         */
        String platformName = projectApplicationService.getPlatformName(request.getProjectId());
        PlatformBugUpdateDTO platformBug = null;
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
           // 平台缺陷, 需同步新增
           ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(request.getProjectId(), true);
           if (serviceIntegration == null) {
               // 项目未配置第三方平台
               throw new MSException(Translator.get("third_party_not_config"));
           }
           // 获取配置平台, 构建平台请求参数, 插入或更新平台缺陷
           Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                   new String(serviceIntegration.getConfiguration()));
           PlatformBugUpdateRequest platformRequest = buildPlatformBugRequest(request);
           platformRequest.setUserPlatformConfig(JSON.toJSONString(userPlatformAccountService.getPluginUserPlatformConfig(serviceIntegration.getPluginId(), currentOrgId, currentUser)));
           platformRequest.setProjectConfig(projectApplicationService.getProjectBugThirdPartConfig(request.getProjectId()));
           if (isUpdate) {
               Bug bug = bugMapper.selectByPrimaryKey(request.getId());
               platformRequest.setPlatformBugId(bug.getPlatformBugId());
               platformBug = platform.updateBug(platformRequest);
           } else {
               platformBug = platform.addBug(platformRequest);
           }
        }
        // 处理基础字段
        Bug bug = handleAndSaveBug(request, currentUser, platformName, platformBug);
        // 处理自定义字段
        handleAndSaveCustomFields(request, isUpdate);
        // 处理附件
        handleAndSaveAttachments(request, files, currentUser, platformName, platformBug);
        // 处理用例关联关系
        handleAndSaveCaseRelation(request, isUpdate, bug, currentUser);

        return bug;
    }

    /**
     * 获取缺陷详情
     * @param id 缺陷ID
     * @return 缺陷详情
     */
    public BugDetailDTO get(String id, String currentUser) {
        Bug bug = checkBugExist(id);
        TemplateDTO template = getTemplate(bug.getTemplateId(), bug.getProjectId(), null, null);
        List<BugCustomFieldDTO> allCustomFields = extBugCustomFieldMapper.getBugAllCustomFields(List.of(id), bug.getProjectId());
        BugDetailDTO detail = new BugDetailDTO();
        detail.setId(id);
        detail.setNum(bug.getNum());
        detail.setProjectId(bug.getProjectId());
        detail.setTemplateId(template.getId());
        detail.setPlatformDefault(template.getPlatformDefault());
        detail.setStatus(bug.getStatus());
        detail.setPlatformBugId(bug.getPlatformBugId());
        detail.setTitle(bug.getTitle());
        if (!detail.getPlatformDefault()) {
            // 非平台默认模板 {内容, 标签, 自定义字段: 处理人, 状态}
            BugContent bugContent = bugContentMapper.selectByPrimaryKey(id);
            detail.setDescription(bugContent.getDescription());
            detail.setTags(bug.getTags());
            template.getCustomFields().forEach(field -> {
                // 状态
                if (StringUtils.equals(field.getFieldKey(), BugTemplateCustomField.STATUS.getId())) {
                    BugCustomFieldDTO status = new BugCustomFieldDTO();
                    status.setId(field.getFieldId());
                    status.setName(field.getFieldName());
                    status.setType(field.getType());
                    status.setValue(bug.getStatus());
                    allCustomFields.addFirst(status);
                }
                // 处理人
                if (StringUtils.equals(field.getFieldKey(), BugTemplateCustomField.HANDLE_USER.getId())) {
                    BugCustomFieldDTO handleUser = new BugCustomFieldDTO();
                    handleUser.setId(field.getFieldId());
                    handleUser.setName(field.getFieldName());
                    handleUser.setType(field.getType());
                    handleUser.setValue(bug.getHandleUser());
                    allCustomFields.addFirst(handleUser);
                }
            });
        } else {
            // 平台默认模板 {自定义字段}
            allCustomFields.forEach(field -> template.getCustomFields().stream().filter(templateField -> StringUtils.equals(templateField.getFieldId(), field.getId())).findFirst().ifPresent(templateField -> {
                field.setName(templateField.getFieldName());
                field.setType(templateField.getType());
            }));
        }
        // 缺陷自定义字段
        detail.setCustomFields(allCustomFields);
        // 缺陷附件信息
        detail.setAttachments(bugAttachmentService.getAllBugFiles(id));
        // 当前登录人是否关注该缺陷
        BugFollowerExample example = new BugFollowerExample();
        example.createCriteria().andBugIdEqualTo(id).andUserIdEqualTo(currentUser);
        detail.setFollowFlag(bugFollowerMapper.countByExample(example) > 0);
        detail.setLinkCaseCount(extBugRelateCaseMapper.countByCaseId(id));
        return detail;
    }

    /**
     * 删除缺陷
     *
     * @param id 缺陷ID
     */
    public void delete(String id, String currentUser) {
        Bug bug = checkById(id);
        if (StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            Bug record = new Bug();
            record.setId(id);
            record.setDeleted(true);
            record.setDeleteUser(currentUser);
            record.setDeleteTime(System.currentTimeMillis());
            bugMapper.updateByPrimaryKeySelective(record);
        } else {
            /*
             * 和当前项目所属平台不一致, 只删除MS缺陷, 不同步删除平台缺陷
             * 一致需同步删除平台缺陷
             */
            String platformName = projectApplicationService.getPlatformName(bug.getProjectId());
            if (StringUtils.equals(platformName, bug.getPlatform())) {
                // 需同步删除平台缺陷
                Platform platform = projectApplicationService.getPlatform(bug.getProjectId(), true);
                platform.deleteBug(bug.getPlatformBugId());
            }
            // 删除缺陷后, 前置操作: 删除关联用例, 删除关联附件
            clearAssociate(id, bug.getProjectId());
            bugMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 恢复缺陷
     * @param id 缺陷ID
     */
    public void recover(String id) {
        Bug bug = checkById(id);
        if (!StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            throw new MSException(NOT_LOCAL_BUG_ERROR);
        }
        Bug record = new Bug();
        record.setId(id);
        record.setDeleted(false);
        bugMapper.updateByPrimaryKeySelective(record);
    }

    /**
     * 彻底删除缺陷
     * @param id 缺陷ID
     */
    public void deleteTrash(String id) {
        Bug bug = checkById(id);
        if (!StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            throw new MSException(NOT_LOCAL_BUG_ERROR);
        }
        bugMapper.deleteByPrimaryKey(id);
    }

    /**
     * 获取缺陷模板详情
     *
     * @param templateId 模板ID
     * @param projectId 项目ID
     * @param platformBugKey 平台缺陷key
     * @return 模板详情
     */
    public TemplateDTO getTemplate(String templateId, String projectId, String fromStatusId, String platformBugKey) {
        Template template = templateMapper.selectByPrimaryKey(templateId);
        if (template != null) {
            // 属于系统模板
            return injectPlatformTemplateBugField(baseTemplateService.getTemplateDTO(template), projectId, fromStatusId, platformBugKey);
        } else {
            // 不属于系统模板
            List<ProjectTemplateOptionDTO> option = projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
            Optional<ProjectTemplateOptionDTO> isThirdPartyDefaultTemplate = option.stream().filter(projectTemplateOptionDTO -> StringUtils.equals(projectTemplateOptionDTO.getId(), templateId)).findFirst();
            if (isThirdPartyDefaultTemplate.isPresent()) {
                // 属于第三方平台默认模板(平台生成的默认模板无需注入配置中的字段)
                return attachTemplateStatusField(getPluginBugDefaultTemplate(projectId, true), projectId, fromStatusId, platformBugKey);
            } else {
                // 不属于系统模板&&不属于第三方平台默认模板, 则该模板已被删除
                return injectPlatformTemplateBugField(projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.BUG.name()), projectId, fromStatusId, platformBugKey);
            }
        }
    }

    /**
     * 批量删除缺陷
     * @param request 请求参数
     */
    public void batchDelete(BugBatchRequest request, String currentUser) {
        List<String> batchIds = getBatchIdsByRequest(request);
        batchIds.forEach(id -> delete(id, currentUser));
        // 批量日志
        List<LogDTO> logs = getBatchLogByRequest(batchIds, OperationLogType.DELETE.name(), "/bug/batch-delete", request.getProjectId(), false, false, null, currentUser);
        operationLogService.batchAdd(logs);
    }

    /**
     * 批量恢复缺陷
     * @param request 请求参数
     */
    public void batchRecover(BugBatchRequest request, String currentUser) {
        List<String> batchIds = getBatchIdsByRequest(request);
        batchIds.forEach(this::recover);
        // 批量日志
        List<LogDTO> logs = getBatchLogByRequest(batchIds, OperationLogType.RECOVER.name(), "/bug/batch-recover", request.getProjectId(), false, false, null, currentUser);
        operationLogService.batchAdd(logs);
    }

    /**
     * 批量彻底删除缺陷
     * @param request 请求参数
     */
    public void batchDeleteTrash(BugBatchRequest request) {
        List<String> batchIds = getBatchIdsByRequest(request);
        batchIds.forEach(this::deleteTrash);
    }

    /**
     * 批量编辑缺陷
     * @param request 请求参数
     * @param currentUser 当前用户
     */
    public void batchUpdate(BugBatchUpdateRequest request, String currentUser) {
        //校验标签长度
        this.checkTagLength(request.getTags());

        List<String> batchIds = getBatchIdsByRequest(request);
        // 批量日志{修改之前}
        List<LogDTO> logs = getBatchLogByRequest(batchIds, OperationLogType.UPDATE.name(), "/bug/batch-update",
                request.getProjectId(), true, request.isAppend(), request.getTags(), currentUser);
        operationLogService.batchAdd(logs);
        // 目前只做标签的批量编辑
        if (request.isAppend()) {
            // 标签(追加)
            List<BugTagEditDTO> bugTagList = extBugMapper.getBugTagList(batchIds);
            Map<String, List<String>> bugTagMap = bugTagList.stream().collect(Collectors.toMap(BugTagEditDTO::getBugId, BugTagEditDTO::getTags));
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            BugMapper batchMapper = sqlSession.getMapper(BugMapper.class);
            bugTagMap.forEach((k, v) -> {
                Bug record = new Bug();
                record.setId(k);
                record.setTags(ListUtils.union(v, request.getTags()));
                record.setUpdateUser(currentUser);
                record.setUpdateTime(System.currentTimeMillis());
                //校验标签长度
                this.checkTagLength(record.getTags());
                //入库
                batchMapper.updateByPrimaryKeySelective(record);
            });
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        } else  {
            // 标签(覆盖)
            request.setUpdateUser(currentUser);
            request.setUpdateTime(System.currentTimeMillis());
            extBugMapper.batchUpdate(request, batchIds);
        }
    }

    /**
     * 拖拽缺陷位置
     * @param request 请求参数
     */
    public void editPos(PosRequest request) {
        ServiceUtils.updatePosField(request,
                Bug.class,
                bugMapper::selectByPrimaryKey,
                extBugMapper::getPrePos,
                extBugMapper::getLastPos,
                bugMapper::updateByPrimaryKeySelective);
    }

    /**
     * 关注缺陷
     * @param id 缺陷ID
     * @param currentUser 当前用户
     */
    public void follow(String id, String currentUser) {
        checkBugExist(id);
        BugFollower bugFollower = new BugFollower();
        bugFollower.setBugId(id);
        bugFollower.setUserId(currentUser);
        bugFollowerMapper.insert(bugFollower);
    }

    /**
     * 取消关注缺陷
     * @param id 缺陷ID
     * @param currentUser 当前用户
     */
    public void unfollow(String id, String currentUser) {
        checkBugExist(id);
        BugFollowerExample example = new BugFollowerExample();
        example.createCriteria().andBugIdEqualTo(id).andUserIdEqualTo(currentUser);
        bugFollowerMapper.deleteByExample(example);
    }

    /**
     * 同步平台缺陷(全量)
     * @param request 同步请求参数
     * @param project 项目
     * @param currentUser 当前用户
     */
    @Async
    public void syncPlatformAllBugs(BugSyncRequest request, Project project, String currentUser) {
        try {
            XpackBugService bugService = CommonBeanFactory.getBean(XpackBugService.class);
            if (bugService != null) {
                bugService.syncPlatformBugs(project, request, currentUser);
            }
        } catch (Exception e) {
            LogUtils.error(e);
            // 同步缺陷异常, 当前同步错误信息 -> Redis(check接口获取)
            bugSyncExtraService.setSyncErrorMsg(request.getProjectId(), e.getMessage());
        } finally {
            // 异常或正常结束都得删除当前项目执行同步的唯一Key
            bugSyncExtraService.deleteSyncKey(request.getProjectId());
        }
    }

    /**
     * 同步平台缺陷(存量)
     * @param remainBugs 存量缺陷
     * @param project 项目
     */
    @Async
    public void syncPlatformBugs(List<Bug> remainBugs, Project project, String currentUser) {
        try {
            // 分页同步
            SubListUtils.dealForSubList(remainBugs, 100, (subBugs) -> doSyncPlatformBugs(subBugs, project));
        } catch (Exception e) {
            LogUtils.error(e);
            // 同步缺陷异常, 当前同步错误信息 -> Redis(check接口获取)
            bugSyncExtraService.setSyncErrorMsg(project.getId(), e.getMessage());
        } finally {
            // 异常或正常结束都得删除当前项目执行同步的Key
            bugSyncExtraService.deleteSyncKey(project.getId());
            // 发送同步通知
            bugSyncNoticeService.sendNotice(remainBugs.size(), currentUser, project.getId());
        }
    }

    /**
     * 执行同步全量缺陷(xpack调用)
     * @param project 项目
     * @param syncRequest 同步请求参数
     */
    @SuppressWarnings("unused")
    public void execSyncAll(Project project, SyncAllBugRequest syncRequest) {
        syncRequest.setProjectConfig(projectApplicationService.getProjectBugThirdPartConfig(project.getId()));
        // 获取平台
        Platform platform = projectApplicationService.getPlatform(project.getId(), true);
        // 同步全量缺陷
        platform.syncAllBugs(syncRequest);
    }

    /**
     * 同步平台缺陷处理
     * @param subBugs 同步的分页缺陷
     * @param project 项目
     */
    private void doSyncPlatformBugs(List<Bug> subBugs, Project project) {
        // 准备参数
        SyncBugRequest request = new SyncBugRequest();
        request.setProjectConfig(projectApplicationService.getProjectBugThirdPartConfig(project.getId()));
        List<PlatformBugDTO> platformBugs = JSON.parseArray(JSON.toJSONString(subBugs), PlatformBugDTO.class);
        List<String> templateIds = platformBugs.stream().map(PlatformBugDTO::getTemplateId).toList();
        List<TemplateCustomField> systemCustomsFields = baseTemplateCustomFieldService.getByTemplateIds(templateIds);
        Map<String, List<TemplateCustomField>> templateFieldMap = systemCustomsFields.stream().collect(Collectors.groupingBy(TemplateCustomField::getTemplateId));
        platformBugs.forEach(platformBug -> {
            platformBug.setPlatformDefaultTemplate(isPluginDefaultTemplate(platformBug.getTemplateId(), project.getId()));
            // 非平台默认模板, 需处理MS模板中映射的字段
            if (!platformBug.getPlatformDefaultTemplate()) {
                List<TemplateCustomField> templateCustomFields = templateFieldMap.get(platformBug.getTemplateId());
                List<PlatformCustomFieldItemDTO> needSyncFields = templateCustomFields.stream().filter(templateCustomField -> StringUtils.isNotBlank(templateCustomField.getApiFieldId())).map(templateCustomField -> {
                    PlatformCustomFieldItemDTO needSyncField = new PlatformCustomFieldItemDTO();
                    needSyncField.setId(templateCustomField.getFieldId());
                    needSyncField.setCustomData(templateCustomField.getApiFieldId());
                    return needSyncField;
                }).toList();
                // 需同步的自定义字段
                platformBug.setNeedSyncCustomFields(needSyncFields);
            }
        });
        request.setBugs(platformBugs);
        // 获取平台
        Platform platform = projectApplicationService.getPlatform(project.getId(), true);
        // 执行同步
        SyncBugResult syncBugResult = platform.syncBugs(request);

        // 处理同步结果
        List<PlatformBugDTO> updateBugs = syncBugResult.getUpdateBug();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            BugMapper batchBugMapper = sqlSession.getMapper(BugMapper.class);
            BugContentMapper batchBugContentMapper = sqlSession.getMapper(BugContentMapper.class);

            // 批量更新缺陷
            updateBugs.forEach(updateBug -> {
                updateBug.setCreateUser(null);
                Bug bug = new Bug();
                BeanUtils.copyBean(bug, updateBug);
                batchBugMapper.updateByPrimaryKeySelective(bug);
                BugContent bugContent = new BugContent();
                bugContent.setBugId(updateBug.getId());
                bugContent.setDescription(updateBug.getDescription());
                batchBugContentMapper.updateByPrimaryKeyWithBLOBs(bugContent);
                // 自定义字段
                BugEditRequest customEditRequest = new BugEditRequest();
                customEditRequest.setId(updateBug.getId());
                customEditRequest.setProjectId(project.getId());
                List<PlatformCustomFieldItemDTO> platformCustomFields = updateBug.getCustomFieldList();
                if (CollectionUtils.isEmpty(platformCustomFields)) {
                    return;
                }
                List<BugCustomFieldDTO> bugCustomFieldDTOList = platformCustomFields.stream().map(platformField -> {
                    BugCustomFieldDTO bugCustomFieldDTO = new BugCustomFieldDTO();
                    bugCustomFieldDTO.setId(platformField.getId());
                    bugCustomFieldDTO.setValue(platformField.getValue() == null ? null : platformField.getValue().toString());
                    return bugCustomFieldDTO;
                }).collect(Collectors.toList());
                customEditRequest.setCustomFields(bugCustomFieldDTOList);
                handleAndSaveCustomFields(customEditRequest, true);
            });

            // 批量删除缺陷
            syncBugResult.getDeleteBugIds().forEach(deleteBugId -> {
                clearAssociate(deleteBugId, project.getId());
                bugMapper.deleteByPrimaryKey(deleteBugId);
            });

            // 同步附件至MS
            if (MapUtils.isNotEmpty(syncBugResult.getAttachmentMap())) {
                bugAttachmentService.syncAttachmentToMs(platform, syncBugResult.getAttachmentMap(), project.getId());
            }

            sqlSession.commit();
        } catch (Exception e) {
            throw new MSException(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    /**
     * 注入平台模板缺陷字段
     * @param templateDTO 模板
     * @param projectId 项目ID
     * @param fromStatusId 起始状态ID
     * @param platformBugKey 平台缺陷key
     * @return 模板
     */
    private TemplateDTO injectPlatformTemplateBugField(TemplateDTO templateDTO, String projectId, String fromStatusId, String platformBugKey) {
        // 来自平台模板
        templateDTO.setPlatformDefault(false);
        String platformName = projectApplicationService.getPlatformName(projectId);

        // 状态字段
        attachTemplateStatusField(templateDTO, projectId, fromStatusId, platformBugKey);

        // 内置字段(处理人字段)
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            // 获取插件中自定义的注入字段(处理人)
            ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
            // 状态选项获取时, 获取平台校验了服务集成配置, 所以此处不需要再次校验
            Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                    new String(serviceIntegration.getConfiguration()));
            List<BugTemplateInjectField> injectFieldList = bugCommonService.getPlatformInjectFields(projectId);
            for (BugTemplateInjectField injectField : injectFieldList) {
                TemplateCustomFieldDTO templateCustomFieldDTO = new TemplateCustomFieldDTO();
                BeanUtils.copyBean(templateCustomFieldDTO, injectField);
                templateCustomFieldDTO.setFieldId(injectField.getId());
                templateCustomFieldDTO.setFieldName(injectField.getName());
                templateCustomFieldDTO.setFieldKey(injectField.getKey());
                GetOptionRequest request = new GetOptionRequest();
                request.setOptionMethod(injectField.getOptionMethod());
                request.setProjectConfig(projectApplicationService.getProjectBugThirdPartConfig(projectId));
                if (StringUtils.equals(injectField.getKey(), BugTemplateCustomField.HANDLE_USER.getId())) {
                    List<SelectOption> formOptions = platform.getFormOptions(request);
                    templateCustomFieldDTO.setOptions(formOptions.stream().map(user -> {
                        CustomFieldOption option = new CustomFieldOption();
                        option.setText(user.getText());
                        option.setValue(user.getValue());
                        return option;
                    }).toList());
                } else {
                    templateCustomFieldDTO.setPlatformOptionJson(JSON.toJSONString(platform.getFormOptions(request)));
                }
                templateDTO.getCustomFields().addFirst(templateCustomFieldDTO);
            }
        } else {
            // Local(处理人)
            TemplateCustomFieldDTO handleUserField = new TemplateCustomFieldDTO();
            handleUserField.setFieldId(BugTemplateCustomField.HANDLE_USER.getId());
            handleUserField.setFieldName(BugTemplateCustomField.HANDLE_USER.getName());
            handleUserField.setFieldKey(BugTemplateCustomField.HANDLE_USER.getId());
            handleUserField.setType(CustomFieldType.SELECT.name());
            handleUserField.setOptions(getMemberOption(projectId));
            handleUserField.setRequired(true);
            templateDTO.getCustomFields().addFirst(handleUserField);
        }

        // 成员类型的自定义字段, 选项值为项目下成员用户
        templateDTO.getCustomFields().forEach(field -> {
            if (StringUtils.equalsAny(field.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                field.setOptions(getMemberOption(projectId));
            }
        });

        return templateDTO;
    }

    /**
     * 注入模板状态字段
     * @param templateDTO 模板
     * @param projectId 项目ID
     * @param fromStatusId 起始状态ID
     * @param platformBugKey 平台缺陷key
     * @return 模板
     */
    public TemplateDTO attachTemplateStatusField(TemplateDTO templateDTO , String projectId, String fromStatusId, String platformBugKey) {
        if (templateDTO == null) {
            return null;
        }
        TemplateCustomFieldDTO statusField = new TemplateCustomFieldDTO();
        statusField.setFieldId(BugTemplateCustomField.STATUS.getId());
        statusField.setFieldName(BugTemplateCustomField.STATUS.getName());
        statusField.setFieldKey(BugTemplateCustomField.STATUS.getId());
        statusField.setType(CustomFieldType.SELECT.name());
        List<SelectOption> statusOption = bugStatusService.getToStatusItemOption(projectId, fromStatusId, platformBugKey);
        List<CustomFieldOption> statusCustomOption = statusOption.stream().map(option -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            customFieldOption.setText(option.getText());
            customFieldOption.setValue(option.getValue());
            return customFieldOption;
        }).toList();
        statusField.setOptions(statusCustomOption);
        statusField.setRequired(true);
        templateDTO.getCustomFields().addFirst(statusField);
        return templateDTO;
    }

    /**
     * 处理保存缺陷基础信息
     *
     * @param request 请求参数
     * @param currentUser 当前用户ID
     * @param platformName 第三方平台名称
     */
    private Bug handleAndSaveBug(BugEditRequest request, String currentUser, String platformName, PlatformBugUpdateDTO platformBug) {
        Bug bug = new Bug();
        BeanUtils.copyBean(bug, request);
        bug.setPlatform(platformName);
        // 状态从内置自定义字段中获取
        Optional<BugCustomFieldDTO> statusField = request.getCustomFields().stream().filter(field -> StringUtils.equals(field.getId(), BugTemplateCustomField.STATUS.getId())).findFirst();
        if (statusField.isPresent()) {
            bug.setStatus(statusField.get().getValue());
            request.getCustomFields().removeIf(field -> StringUtils.equals(field.getId(), BugTemplateCustomField.STATUS.getId()));
        } else {
            throw new MSException(Translator.get("bug_status_can_not_be_empty"));
        }

        // 设置基础字段
        if (StringUtils.equalsIgnoreCase(BugPlatform.LOCAL.getName(), platformName)) {
            bug.setPlatformBugId(null);
            // Local缺陷处理人从自定义字段中获取
            Optional<BugCustomFieldDTO> handleUserField = request.getCustomFields().stream().filter(field -> StringUtils.equals(field.getId(), BugTemplateCustomField.HANDLE_USER.getId())).findFirst();
            if (handleUserField.isPresent()) {
                bug.setHandleUser(handleUserField.get().getValue());
                request.getCustomFields().removeIf(field -> StringUtils.equals(field.getId(), BugTemplateCustomField.HANDLE_USER.getId()));
            } else {
                throw new MSException(Translator.get("handle_user_can_not_be_empty"));
            }
        } else {
            bug.setPlatformBugId(platformBug.getPlatformBugKey());
            if (StringUtils.isNotBlank(platformBug.getPlatformTitle())) {
                bug.setTitle(platformBug.getPlatformTitle());
            }
            if (StringUtils.isNotBlank(platformBug.getPlatformDescription())) {
                request.setDescription(platformBug.getPlatformDescription());
            }
            if (StringUtils.isNotBlank(platformBug.getPlatformHandleUser())) {
                bug.setHandleUser(platformBug.getPlatformHandleUser());
            } else {
                // 平台处理人为空
                bug.setHandleUser(StringUtils.EMPTY);
            }
            if (StringUtils.isNotBlank(platformBug.getPlatformStatus())) {
                bug.setStatus(platformBug.getPlatformStatus());
            } else {
                // 平台状态为空
                bug.setStatus(StringUtils.EMPTY);
            }
            // 第三方平台内置的处理人字段需要从自定义字段中移除
            request.getCustomFields().removeIf(field -> StringUtils.startsWith(field.getName(), BugTemplateCustomField.HANDLE_USER.getName()));
        }

        //保存基础信息
        if (StringUtils.isEmpty(bug.getId())) {
            bug.setId(IDGenerator.nextStr());
            bug.setNum(Long.valueOf(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.BUG_MANAGEMENT)).intValue());
            bug.setHandleUsers(bug.getHandleUser());
            bug.setCreateUser(currentUser);
            bug.setCreateTime(System.currentTimeMillis());
            bug.setUpdateUser(currentUser);
            bug.setUpdateTime(System.currentTimeMillis());
            bug.setDeleted(false);
            bug.setPos(getNextPos(request.getProjectId()));
            bugMapper.insert(bug);
            request.setId(bug.getId());
            BugContent bugContent = new BugContent();
            bugContent.setBugId(bug.getId());
            bugContent.setDescription(StringUtils.isEmpty(request.getDescription()) ? StringUtils.EMPTY : request.getDescription());
            bugContentMapper.insert(bugContent);
        } else {
            Bug orignalBug = checkBugExist(request.getId());
            // 追加处理人
            if (!StringUtils.equals(orignalBug.getHandleUser(), bug.getHandleUser())) {
                bug.setHandleUsers(orignalBug.getHandleUsers() + "," + bug.getHandleUser());
            }
            bug.setUpdateUser(currentUser);
            bug.setUpdateTime(System.currentTimeMillis());
            bugMapper.updateByPrimaryKeySelective(bug);
            BugContent bugContent = new BugContent();
            bugContent.setBugId(bug.getId());
            bugContent.setDescription(StringUtils.isEmpty(request.getDescription()) ? StringUtils.EMPTY : request.getDescription());
            bugContentMapper.updateByPrimaryKeySelective(bugContent);
        }
        return bug;
    }

    /**
     * 校验缺陷是否存在
     * @param id 缺陷ID
     * @return 缺陷
     */
    private Bug checkBugExist(String id) {
        BugExample bugExample = new BugExample();
        bugExample.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
        List<Bug> bugs = bugMapper.selectByExample(bugExample);
        if (CollectionUtils.isEmpty(bugs)) {
            throw new MSException(BUG_NOT_EXIST);
        }
        return bugs.get(0);
    }

    /**
     * 校验缺陷是否存在并返回
     * @param id 缺陷ID
     * @return 缺陷
     */
    public boolean checkExist(String id) {
        BugExample bugExample = new BugExample();
        bugExample.createCriteria().andIdEqualTo(id).andDeletedEqualTo(false);
        return bugMapper.countByExample(bugExample) > 0;
    }

    /**
     * 处理保存自定义字段信息
     *
     * @param request 请求参数
     */
    public void handleAndSaveCustomFields(BugEditRequest request, boolean merge) {
        // 处理ID, 值的映射关系
        Map<String, String> customFieldMap = request.getCustomFields().stream().collect(HashMap::new, (m, field) -> m.put(field.getId(), field.getValue()), HashMap::putAll);
        if (MapUtils.isEmpty(customFieldMap)) {
            return;
        }
        List<BugCustomField> addFields = new ArrayList<>();
        List<BugCustomField> updateFields = new ArrayList<>();
        if (merge) {
            // 编辑缺陷需合并原有自定义字段
            List<BugCustomFieldDTO> originalFields = extBugCustomFieldMapper.getBugAllCustomFields(List.of(request.getId()), request.getProjectId());
            Map<String, String> originalFieldMap = originalFields.stream().collect(Collectors.toMap(BugCustomFieldDTO::getId, field -> Optional.ofNullable(field.getValue()).orElse(StringUtils.EMPTY)));
            customFieldMap.keySet().forEach(fieldId -> {
                BugCustomField bugCustomField = new BugCustomField();
                if (!originalFieldMap.containsKey(fieldId)) {
                    // 新的缺陷字段关系
                    bugCustomField.setBugId(request.getId());
                    bugCustomField.setFieldId(fieldId);
                    bugCustomField.setValue(customFieldMap.get(fieldId));
                    addFields.add(bugCustomField);
                } else {
                    // 已存在的缺陷字段关系
                    bugCustomField.setBugId(request.getId());
                    bugCustomField.setFieldId(fieldId);
                    bugCustomField.setValue(customFieldMap.get(fieldId));
                    updateFields.add(bugCustomField);
                }
            });
        } else {
            // 新增缺陷不需要合并自定义字段
            customFieldMap.keySet().forEach(fieldId -> {
                BugCustomField bugCustomField = new BugCustomField();
                bugCustomField.setBugId(request.getId());
                bugCustomField.setFieldId(fieldId);
                bugCustomField.setValue(customFieldMap.get(fieldId));
                addFields.add(bugCustomField);
            });
        }
        if (CollectionUtils.isNotEmpty(addFields)) {
            bugCustomFieldMapper.batchInsert(addFields);
        }
        if (CollectionUtils.isNotEmpty(updateFields)) {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            BugCustomFieldMapper bugCustomFieldMapper = sqlSession.getMapper(BugCustomFieldMapper.class);
            for (BugCustomField bugCustomField : updateFields) {
                BugCustomFieldExample bugCustomFieldExample = new BugCustomFieldExample();
                bugCustomFieldExample.createCriteria().andBugIdEqualTo(bugCustomField.getBugId()).andFieldIdEqualTo(bugCustomField.getFieldId());
                bugCustomFieldMapper.updateByExample(bugCustomField, bugCustomFieldExample);
            }
            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    /**
     * 处理保存附件信息
     * @param request 请求参数
     * @param files 上传附件集合
     */
    private void handleAndSaveAttachments(BugEditRequest request, List<MultipartFile> files, String currentUser, String platformName, PlatformBugUpdateDTO platformBug) {
        /*
         * 附件处理逻辑 (注意: 第三方平台缺陷需同步这些附件)
         * 1. 先处理删除, 及取消关联的附件
         * 2. 再处理新上传的, 新关联的附件
         */
        // 同步删除附件集合
        List<SyncAttachmentToPlatformRequest> removeAttachments = removeAttachment(request, platformBug, currentUser, platformName);
        // 同步上传附件集合
        List<SyncAttachmentToPlatformRequest> uploadAttachments = uploadAttachment(request, files, platformBug, currentUser, platformName);
        // 附件汇总
        List<SyncAttachmentToPlatformRequest> allSyncAttachments = Stream.concat(removeAttachments.stream(), uploadAttachments.stream()).toList();

        // 同步至第三方(异步调用)
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName()) && CollectionUtils.isNotEmpty(allSyncAttachments)) {
            bugPlatformService.syncAttachmentToPlatform(allSyncAttachments, request.getProjectId());
        }
    }

    /**
     * 移除缺陷附件
     * @param request 请求参数
     * @param platformBug 平台缺陷
     * @param currentUser 当前用户
     * @param platformName 平台名称
     * @return 同步删除附件集合
     */
    private List<SyncAttachmentToPlatformRequest> removeAttachment(BugEditRequest request, PlatformBugUpdateDTO platformBug, String currentUser,
                                                                   String platformName) {
        List<SyncAttachmentToPlatformRequest> removePlatformAttachments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getDeleteLocalFileIds())) {
            BugLocalAttachmentExample example = new BugLocalAttachmentExample();
            example.createCriteria().andBugIdEqualTo(request.getId()).andFileIdIn(request.getDeleteLocalFileIds());
            List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentMapper.selectByExample(example);
            Map<String, BugLocalAttachment> localAttachmentMap = bugLocalAttachments.stream().collect(Collectors.toMap(BugLocalAttachment::getFileId, v -> v));
            // 删除本地上传的附件, BUG_LOCAL_ATTACHMENT表
            request.getDeleteLocalFileIds().forEach(deleteFileId -> {
                FileRequest fileRequest = buildBugFileRequest(request.getProjectId(), request.getId(), deleteFileId, localAttachmentMap.get(deleteFileId).getFileName());
                try {
                    fileService.deleteFile(fileRequest);
                    // 删除的本地的附件同步至平台
                    if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                        File deleteTmpFile = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(StringUtils.EMPTY)).getPath() + File.separator + "tmp"
                                + File.separator + localAttachmentMap.get(deleteFileId).getFileName());
                        removePlatformAttachments.add(new SyncAttachmentToPlatformRequest(platformBug.getPlatformBugKey(), deleteTmpFile, SyncAttachmentType.DELETE.syncOperateType()));
                    }
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_delete_error"));
                }
            });
            bugLocalAttachmentMapper.deleteByExample(example);
        }
        if (CollectionUtils.isNotEmpty(request.getUnLinkRefIds())) {
            FileAssociationExample example = new FileAssociationExample();
            example.createCriteria().andIdIn(request.getUnLinkRefIds());
            List<FileAssociation> fileAssociations = fileAssociationMapper.selectByExample(example);
            List<String> metaIds = fileAssociations.stream().map(FileAssociation::getFileId).toList();
            FileMetadataExample metadataExample = new FileMetadataExample();
            metadataExample.createCriteria().andIdIn(metaIds);
            List<FileMetadata> fileMetadataList = fileMetadataMapper.selectByExample(metadataExample);
            fileMetadataList.forEach(fileMetadata -> {
                // 取消关联的附件同步至平台
                if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                    File deleteTmpFile = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(StringUtils.EMPTY)).getPath() + File.separator + "tmp"
                            + File.separator + fileMetadata.getName() + "." + fileMetadata.getType());
                    removePlatformAttachments.add(new SyncAttachmentToPlatformRequest(platformBug.getPlatformBugKey(), deleteTmpFile, SyncAttachmentType.DELETE.syncOperateType()));
                }
            });
            // 取消关联的附件, FILE_ASSOCIATION表
            fileAssociationService.deleteByIds(request.getUnLinkRefIds(), createFileLogRecord(currentUser, request.getProjectId()));
        }
        return removePlatformAttachments;
    }

    /**
     * 上传缺陷附件
     * @param request 请求参数
     * @param files 上传的文件集合
     * @param platformBug 平台缺陷
     * @param currentUser 当前用户
     * @param platformName 平台名称
     * @return 同步删除附件集合
     */
    private List<SyncAttachmentToPlatformRequest> uploadAttachment(BugEditRequest request, List<MultipartFile> files, PlatformBugUpdateDTO platformBug,
                                                                   String currentUser, String platformName) {
        List<SyncAttachmentToPlatformRequest> uploadPlatformAttachments = new ArrayList<>();
        // 复制的附件
        List<BugLocalAttachment> copyFiles = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getCopyFiles())) {
            // 本地附件
            request.getCopyFiles().stream().filter(BugFileDTO::getLocal).forEach(localFile -> {
                try {
                    BugFileSourceRequest sourceRequest = new BugFileSourceRequest();
                    sourceRequest.setBugId(localFile.getBugId());
                    sourceRequest.setProjectId(request.getProjectId());
                    sourceRequest.setFileId(localFile.getFileId());
                    sourceRequest.setAssociated(false);
                    byte[] bytes = bugAttachmentService.downloadOrPreview(sourceRequest).getBody();
                    if (bytes != null) {
                        BugLocalAttachment localAttachment = buildBugLocalAttachment(request.getId(), localFile.getFileName(), bytes.length, currentUser);
                        copyFiles.add(localAttachment);
                        // 上传文件库
                        FileCenter.getDefaultRepository().saveFile(bytes, buildBugFileRequest(request.getProjectId(), request.getId(), localAttachment.getFileId(), localFile.getFileName()));
                        // 同步新上传的附件至平台
                        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                            File uploadTmpFile = new File(LocalRepositoryDir.getBugTmpDir() + "/" + localFile.getFileName());
                            FileUtils.writeByteArrayToFile(uploadTmpFile, bytes);
                            uploadPlatformAttachments.add(new SyncAttachmentToPlatformRequest(platformBug.getPlatformBugKey(), uploadTmpFile, SyncAttachmentType.UPLOAD.syncOperateType()));
                        }
                    }
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_upload_error"));
                }
            });
            if (CollectionUtils.isNotEmpty(copyFiles)) {
                extBugLocalAttachmentMapper.batchInsert(copyFiles);
            }
            // 关联的附件, 直接合并, 后续逻辑会处理
            List<String> copyLinkFileIds = request.getCopyFiles().stream().filter(file -> !file.getLocal()).map(BugFileDTO::getFileId).collect(Collectors.toList());
            request.setLinkFileIds(ListUtils.union(request.getLinkFileIds(), copyLinkFileIds));
        }
        // 新本地上传的附件
        List<BugLocalAttachment> addFiles = new ArrayList<>();
        Map<String, MultipartFile> uploadMinioFiles = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                BugLocalAttachment localAttachment = buildBugLocalAttachment(request.getId(), file.getOriginalFilename(), file.getSize(), currentUser);
                addFiles.add(localAttachment);
                uploadMinioFiles.put(localAttachment.getFileId(), file);
            });
            extBugLocalAttachmentMapper.batchInsert(addFiles);
            uploadMinioFiles.forEach((fileId, file) -> {
                FileRequest fileRequest = buildBugFileRequest(request.getProjectId(), request.getId(), fileId, file.getOriginalFilename());
                try {
                    fileService.upload(file, fileRequest);
                    // 同步新上传的附件至平台
                    if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                        File uploadTmpFile = new File(LocalRepositoryDir.getBugTmpDir() + "/" +  file.getOriginalFilename());
                        FileUtils.writeByteArrayToFile(uploadTmpFile, file.getBytes());
                        uploadPlatformAttachments.add(new SyncAttachmentToPlatformRequest(platformBug.getPlatformBugKey(), uploadTmpFile, SyncAttachmentType.UPLOAD.syncOperateType()));
                    }
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_upload_error"));
                }
            });
        }
        // 新关联的附件
        if (CollectionUtils.isNotEmpty(request.getLinkFileIds())) {
            fileAssociationService.association(request.getId(), FileAssociationSourceUtil.SOURCE_TYPE_BUG, request.getLinkFileIds(),
                    createFileLogRecord(currentUser, request.getProjectId()));
            // 同步新关联的附件至平台
            if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                FileMetadataExample fileMetadataExample = new FileMetadataExample();
                fileMetadataExample.createCriteria().andIdIn(request.getLinkFileIds());
                List<FileMetadata> fileMetadata = fileMetadataMapper.selectByExample(fileMetadataExample);
                Map<String, FileMetadata> fileMetadataMap = fileMetadata.stream().collect(Collectors.toMap(FileMetadata::getId, v -> v));
                request.getLinkFileIds().forEach(fileId -> {
                    // 平台同步附件集合
                    FileMetadata meta = fileMetadataMap.get(fileId);
                    if (meta != null) {
                        try {
                            File uploadTmpFile = new File(LocalRepositoryDir.getBugTmpDir() + "/" + meta.getName() + "." + meta.getType());
                            byte[] fileByte = fileMetadataService.getFileByte(meta);
                            FileUtils.writeByteArrayToFile(uploadTmpFile, fileByte);
                            uploadPlatformAttachments.add(new SyncAttachmentToPlatformRequest(platformBug.getPlatformBugKey(), uploadTmpFile, SyncAttachmentType.UPLOAD.syncOperateType()));
                        } catch (IOException e) {
                            throw new MSException(Translator.get("bug_attachment_link_error"));
                        }
                    }
                });
            }
        }
        return uploadPlatformAttachments;
    }

    /**
     * 处理并保存缺陷用例关联关系
     * @param request 请求参数
     * @param isUpdate 是否更新
     * @param bug 缺陷
     * @param currentUser 当前用户
     */
    private void handleAndSaveCaseRelation(BugEditRequest request, boolean isUpdate, Bug bug, String currentUser) {
        if (!isUpdate && StringUtils.isNotBlank(request.getCaseId())) {
            //用例创建缺陷并关联
            BugRelationCase bugRelationCase = new BugRelationCase();
            bugRelationCase.setId(IDGenerator.nextStr());
            bugRelationCase.setCaseId(request.getCaseId());
            bugRelationCase.setBugId(bug.getId());
            bugRelationCase.setCaseType(CaseType.FUNCTIONAL_CASE.getKey());
            bugRelationCase.setCreateUser(currentUser);
            bugRelationCase.setCreateTime(System.currentTimeMillis());
            bugRelationCase.setUpdateTime(System.currentTimeMillis());
            bugRelationCaseMapper.insertSelective(bugRelationCase);
        }
    }

   /**
    * 封装缺陷平台请求参数
    * @param request 缺陷请求参数
    */
   private PlatformBugUpdateRequest buildPlatformBugRequest(BugEditRequest request) {
       PlatformBugUpdateRequest platformRequest = new PlatformBugUpdateRequest();
       /*
        * 处理平台自定义字段
        * 参数中模板非平台默认模板, 则为系统自定义模板, 只需处理配置API映射的字段
        */
       TemplateDTO pluginDefaultTemplate = getPluginBugDefaultTemplate(request.getProjectId(), false);
       // 参数模板为插件默认模板, 处理所有自定义字段, 无需过滤API映射
       boolean noApiFilter = pluginDefaultTemplate != null && StringUtils.equals(pluginDefaultTemplate.getId(), request.getTemplateId());
       platformRequest.setCustomFieldList(transferCustomToPlatformField(request.getTemplateId(), request.getCustomFields(), noApiFilter));
       // TITLE, DESCRIPTION 传到平台插件处理
       platformRequest.setTitle(request.getTitle());
       platformRequest.setDescription(request.getDescription());
       return platformRequest;
   }

    /**
     * 是否插件默认模板
     * @param templateId 模板ID
     * @param projectId 项目ID
     * @return 是否插件默认模板
     */
   private boolean isPluginDefaultTemplate(String templateId, String projectId) {
       Template pluginTemplate = projectTemplateService.getPluginBugTemplate(projectId);
       return pluginTemplate != null && StringUtils.equals(pluginTemplate.getId(), templateId);
   }

    /**
     * 封装缺陷其他字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    public List<BugDTO> buildExtraInfo(List<BugDTO> bugs) {
        // 获取用户集合
        List<String> userIds = new ArrayList<>(bugs.stream().map(BugDTO::getCreateUser).toList());
        userIds.addAll(bugs.stream().map(BugDTO::getUpdateUser).toList());
        userIds.addAll(bugs.stream().map(BugDTO::getDeleteUser).toList());
        userIds.addAll(bugs.stream().map(BugDTO::getHandleUser).toList());
        List<String> distinctUserIds = userIds.stream().distinct().toList();
        List<OptionDTO> userOptions = baseUserMapper.selectUserOptionByIds(distinctUserIds);
        Map<String, String> userMap = userOptions.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        // 根据缺陷ID获取关联用例数
        List<String> ids = bugs.stream().map(BugDTO::getId).toList();
        List<BugRelateCaseCountDTO> relationCaseCount = extBugRelateCaseMapper.countRelationCases(ids);
        Map<String, Integer> countMap = relationCaseCount.stream().collect(Collectors.toMap(BugRelateCaseCountDTO::getBugId, BugRelateCaseCountDTO::getRelationCaseCount));
        bugs.forEach(bug -> {
            bug.setRelationCaseCount(countMap.get(bug.getId()) == null ? 0 : countMap.get(bug.getId()));
            bug.setCreateUserName(userMap.get(bug.getCreateUser()));
            bug.setUpdateUserName(userMap.get(bug.getUpdateUser()));
            bug.setDeleteUserName(userMap.get(bug.getDeleteUser()));
        });
        return bugs;
    }

    /**
     * 处理自定义字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    public List<BugDTO> handleCustomField(List<BugDTO> bugs, String projectId) {
        List<String> ids = bugs.stream().map(BugDTO::getId).toList();
        List<BugCustomFieldDTO> customFields = extBugCustomFieldMapper.getBugAllCustomFields(ids, projectId);
        Map<String, List<BugCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(BugCustomFieldDTO::getBugId));
        // 表头处理人选项
        List<SelectOption> handleUserOption = bugCommonService.getHeaderHandlerOption(projectId);
        Map<String, String> handleMap = handleUserOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        List<SelectOption> localHandlerOption = bugCommonService.getLocalHandlerOption(projectId);
        Map<String, String> localHandleMap = localHandlerOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        // 表头状态选项
        List<SelectOption> statusOption = bugStatusService.getHeaderStatusOption(projectId);
        Map<String, String> statusMap = statusOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        List<SelectOption> localStatusOptions = bugStatusService.getAllLocalStatusOptions(projectId);
        Map<String, String> localStatusMap = localStatusOptions.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        bugs.forEach(bug -> {
            bug.setCustomFields(customFieldMap.get(bug.getId()));
            // 解析处理人, 状态
            bug.setHandleUserName(StringUtils.isBlank(handleMap.get(bug.getHandleUser())) ? localHandleMap.get(bug.getHandleUser()) : handleMap.get(bug.getHandleUser()));
            bug.setStatusName(StringUtils.isBlank(statusMap.get(bug.getStatus())) ? localStatusMap.get(bug.getStatus()) : statusMap.get(bug.getStatus()));
        });
        return bugs;
    }


    /**
     * 自定义字段转换为平台字段
     * @param templateId 模板ID
     * @param customFields 自定义字段集合
     * @param noApiFilter 是否不过滤API映射的字段
     * @return 平台字段集合
     */
    public List<PlatformCustomFieldItemDTO> transferCustomToPlatformField(String templateId, List<BugCustomFieldDTO> customFields, boolean noApiFilter) {
        List<BugCustomFieldDTO> platformCustomFields = new ArrayList<>(customFields);
        if (!noApiFilter) {
            // 过滤出API映射的字段
            List<TemplateCustomField> systemCustomsFields = baseTemplateCustomFieldService.getByTemplateId(templateId);
            Map<String, String> systemCustomFieldApiMap;
            if (CollectionUtils.isNotEmpty(systemCustomsFields)) {
                systemCustomFieldApiMap = systemCustomsFields.stream().collect(Collectors.toMap(TemplateCustomField::getFieldId, f -> Optional.ofNullable(f.getApiFieldId()).orElse(StringUtils.EMPTY)));
                // 移除除状态, 处理人以外的所有非API映射的字段
                platformCustomFields.removeIf(field -> systemCustomFieldApiMap.containsKey(field.getId()) && StringUtil.isBlank(systemCustomFieldApiMap.get(field.getId())));
            } else {
                systemCustomFieldApiMap = new HashMap<>();
            }
            return platformCustomFields.stream().map(field -> {
                PlatformCustomFieldItemDTO platformCustomFieldItem = new PlatformCustomFieldItemDTO();
                platformCustomFieldItem.setName(field.getName());
                platformCustomFieldItem.setCustomData(systemCustomFieldApiMap.containsKey(field.getId()) ? systemCustomFieldApiMap.get(field.getId()) : field.getId());
                platformCustomFieldItem.setValue(field.getValue());
                platformCustomFieldItem.setType(field.getType());
                return platformCustomFieldItem;
            }).collect(Collectors.toList());
        } else {
            // 平台默认模板, 处理所有自定义字段
            return platformCustomFields.stream().map(field -> {
                PlatformCustomFieldItemDTO platformCustomFieldItem = new PlatformCustomFieldItemDTO();
                platformCustomFieldItem.setName(field.getName());
                platformCustomFieldItem.setCustomData(field.getId());
                platformCustomFieldItem.setValue(field.getValue());
                platformCustomFieldItem.setType(field.getType());
                return platformCustomFieldItem;
            }).collect(Collectors.toList());
        }
    }

   /**
    * 获取第三方平台默认模板
    *
    * @param projectId 项目ID
    * @return 第三方平台默认模板
    */
   private TemplateDTO getPluginBugDefaultTemplate(String projectId, boolean setPluginTemplateField) {
       // 在获取插件模板之前, 已经获取过平台集成信息, 这里不再判空
       ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
       TemplateDTO template = new TemplateDTO();
       Template pluginTemplate = projectTemplateService.getPluginBugTemplate(projectId);
       if (pluginTemplate == null) {
           return null;
       }
       BeanUtils.copyBean(template, pluginTemplate);
       if (setPluginTemplateField) {
           Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                   new String(serviceIntegration.getConfiguration()));
           String projectConfig = projectApplicationService.getProjectBugThirdPartConfig(projectId);
           List<PlatformCustomFieldItemDTO> platformCustomFields = new ArrayList<>();
           try {
               platformCustomFields = platform.getDefaultTemplateCustomField(projectConfig);
           } catch (Exception e) {
               LogUtils.error("获取平台默认模板字段失败: " + e.getMessage());
           }
           if (CollectionUtils.isNotEmpty(platformCustomFields)) {
               List<TemplateCustomFieldDTO> customFields = platformCustomFields.stream().map(platformCustomField -> {
                   TemplateCustomFieldDTO customField = new TemplateCustomFieldDTO();
                   BeanUtils.copyBean(customField, platformCustomField);
                   customField.setFieldId(platformCustomField.getId());
                   customField.setFieldName(platformCustomField.getName());
                   customField.setPlatformOptionJson(platformCustomField.getOptions());
                   customField.setPlatformPlaceHolder(platformCustomField.getPlaceHolder());
                   customField.setPlatformSystemField(platformCustomField.getSystemField());
                   return customField;
               }).collect(Collectors.toList());
               template.setCustomFields(customFields);
           }
       }
       // 平台插件中获取的默认模板
       template.setPlatformDefault(true);
       return template;
   }

   /**
    * 清空关联信息
    * @param bugId 缺陷ID
    * @param projectId 项目ID
    */
   public void clearAssociate(String bugId, String projectId) {
       // 清空附件关系及本地附件
       FileAssociationExample example = new FileAssociationExample();
       example.createCriteria().andSourceIdEqualTo(bugId).andSourceTypeEqualTo(FileAssociationSourceUtil.SOURCE_TYPE_BUG);
       fileAssociationMapper.deleteByExample(example);
       BugLocalAttachmentExample attachmentExample = new BugLocalAttachmentExample();
       attachmentExample.createCriteria().andBugIdEqualTo(bugId);
       List<BugLocalAttachment> bugLocalAttachments = bugLocalAttachmentMapper.selectByExample(attachmentExample);
       bugLocalAttachments.forEach(bugLocalAttachment -> {
           FileRequest fileRequest = buildBugFileRequest(projectId, bugId, bugLocalAttachment.getFileId(), bugLocalAttachment.getFileName());
           try {
               fileService.deleteFile(fileRequest);
           } catch (Exception e) {
               throw new MSException(e);
           }
       });
       bugLocalAttachmentMapper.deleteByExample(attachmentExample);
       // 清空关联用例
       BugRelationCaseExample relationCaseExample = new BugRelationCaseExample();
       relationCaseExample.createCriteria().andBugIdEqualTo(bugId);
       bugRelationCaseMapper.deleteByExample(relationCaseExample);
       // 清除自定义字段关系
       BugCustomFieldExample customFieldExample = new BugCustomFieldExample();
       customFieldExample.createCriteria().andBugIdEqualTo(bugId);
       bugCustomFieldMapper.deleteByExample(customFieldExample);
       // 清空缺陷内容
       BugContentExample contentExample = new BugContentExample();
       contentExample.createCriteria().andBugIdEqualTo(bugId);
       bugContentMapper.deleteByExample(contentExample);
   }

    /**
     *
     * @param operator 操作人
     * @param projectId 项目ID
     * @return 文件操作日志记录
     */
    private FileLogRecord createFileLogRecord(String operator, String projectId){
        return FileLogRecord.builder()
                .logModule(OperationLogModule.BUG_MANAGEMENT_INDEX)
                .operator(operator)
                .projectId(projectId)
                .build();
    }

    /**
     * 构建缺陷文件请求
     * @param projectId 项目ID
     * @param resourceId 资源ID
     * @param fileId 文件ID
     * @param fileName 文件名称
     * @return 文件请求对象
     */
    private FileRequest buildBugFileRequest(String projectId, String resourceId, String fileId, String fileName) {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getBugDir(projectId, resourceId) + "/" + fileId);
        fileRequest.setFileName(StringUtils.isEmpty(fileName) ? null : fileName);
        fileRequest.setStorage(StorageType.MINIO.name());
        return fileRequest;
    }

    /**
     * 导出缺陷
     * @param request 导出请求参数
     * @return 导出对象
     * @throws Exception 异常
     */
    public ResponseEntity<byte[]> export(BugExportRequest request) throws Exception {
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        // 准备导出缺陷, 自定义字段, 自定义字段选项值
        List<BugDTO> bugs = this.getExportDataByBatchRequest(request);
        if (CollectionUtils.isEmpty(bugs)) {
            throw new MSException(Translator.get("no_bug_select"));
        }
        // 缺陷自定义字段内容及补充内容
        handleCustomField(bugs, request.getProjectId());
        bugs = buildExtraInfo(bugs);
        // 表头处理人选项
        List<SelectOption> handleUserOption = bugCommonService.getHeaderHandlerOption(request.getProjectId());
        Map<String, String> handleUserMap = handleUserOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        // 表头状态选项
        List<SelectOption> statusOption = bugStatusService.getHeaderStatusOption(request.getProjectId());
        Map<String, String> statusMap = statusOption.stream().collect(Collectors.toMap(SelectOption::getValue, SelectOption::getText));
        // 表头自定义字段
        List<TemplateCustomFieldDTO> headerCustomFields = getHeaderCustomFields(request.getProjectId());
        String xlsxFileNamePrefix = "MeterSphere_bug_" + project.getName() + "_";
        ExportUtils exportUtils = new ExportUtils(bugs,
                BugExportHeaderModel.builder()
                        .exportColumns(request.getExportColumns())
                        .headerCustomFields(headerCustomFields)
                        .handleUserMap(handleUserMap).statusMap(statusMap)
                        .xlsxFileNamePrefix(xlsxFileNamePrefix).build());
        // 导出
        byte[] bytes = exportUtils.exportToZipFile(bugExportService::generateExcelFiles);
        String zipName = "MeterSphere_bug_" + URLEncoder.encode(project.getName(), StandardCharsets.UTF_8) + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipName + "\";" + "filename*=utf-8''"+ zipName)
                .body(bytes);
    }

    /**
     * 获取导出列
     * @param projectId 项目ID
     * @return 缺陷导出列
     */
    public BugExportColumns getExportColumns(String projectId) {
        BugExportColumns bugExportColumns = new BugExportColumns();
        // 表头自定义字段
        List<TemplateCustomFieldDTO> headerCustomFields = getHeaderCustomFields(projectId);
        bugExportColumns.initCustomColumns(headerCustomFields);
        return bugExportColumns;
    }

    /**
     * 获取批量导出的缺陷集合
     * @param request 批量操作参数
     * @return 缺陷集合
     */
    private List<BugDTO> getExportDataByBatchRequest(BugBatchRequest request) {
        if (request.isSelectAll()) {
            // 全选{根据查询条件查询所有数据, 排除取消勾选的数据}
            BugPageRequest bugPageRequest = new BugPageRequest();
            BeanUtils.copyBean(bugPageRequest, request);
            bugPageRequest.setUseTrash(false);
            List<BugDTO> allBugs = extBugMapper.list(bugPageRequest, request.getSort());
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                allBugs.removeIf(bug -> request.getExcludeIds().contains(bug.getId()));
            }
            return allBugs;
        } else {
            // 部分勾选
            if (CollectionUtils.isEmpty(request.getSelectIds())) {
                throw new MSException(Translator.get("no_bug_select"));
            }
            return extBugMapper.listByIds(request.getSelectIds(), request.getSort());
        }
    }

    /**
     * 获取批量操作的缺陷ID集合
     * @param request 批量操作参数
     * @return 缺陷集合
     */
    public List<String> getBatchIdsByRequest(BugBatchRequest request) {
        if (request.isSelectAll()) {
            // 全选{根据查询条件查询所有数据, 排除取消勾选的数据}
            BugPageRequest bugPageRequest = new BugPageRequest();
            BeanUtils.copyBean(bugPageRequest, request);
            List<String> ids = extBugMapper.getIdsByPageRequest(bugPageRequest);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeIf(id -> request.getExcludeIds().contains(id));
            }
            if (CollectionUtils.isEmpty(ids)) {
                throw new MSException(Translator.get("no_bug_select"));
            }
            return ids;
        } else {
            // 部分勾选
            if (CollectionUtils.isEmpty(request.getSelectIds())) {
                throw new MSException(Translator.get("no_bug_select"));
            }
            return request.getSelectIds();
        }
    }

    /**
     * 获取表头自定义字段
     * @param projectId 项目ID
     * @return 自定义字段集合
     */
    public List<TemplateCustomFieldDTO> getHeaderCustomFields(String projectId) {
        List<TemplateCustomFieldDTO> allCustomFields = new ArrayList<>();
        // 本地模板
        List<Template> templates = projectTemplateService.getTemplates(projectId, TemplateScene.BUG.name());
        templates.forEach(template -> allCustomFields.addAll(baseTemplateService.getTemplateDTO(template).getCustomFields()));
        // 本地模板自定义字段去重
        List<TemplateCustomFieldDTO> headerCustomFields = allCustomFields.stream().filter(distinctByKey(TemplateCustomFieldDTO::getFieldId)).collect(Collectors.toList());
        // 填充自定义字段成员类型的选项值
        List<SelectOption> memberOption = bugCommonService.getHeaderHandlerOption(projectId);
        List<CustomFieldOption> memberCustomOption = memberOption.stream().map(option -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            customFieldOption.setValue(option.getValue());
            customFieldOption.setText(option.getText());
            return customFieldOption;
        }).toList();
        headerCustomFields.forEach(field -> {
            if (StringUtils.equalsAny(field.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                field.setOptions(memberCustomOption);
            }
        });
        // 第三方平台模板
        TemplateDTO pluginDefaultTemplate = getPluginBugDefaultTemplate(projectId, true);
        if (pluginDefaultTemplate != null) {
            headerCustomFields.addAll(pluginDefaultTemplate.getCustomFields());
        }
        return headerCustomFields;
    }

    /**
     * 校验缺陷是否存在并返回
     * @param bugId 缺陷ID
     * @return 缺陷
     */
    private Bug checkById(String bugId) {
        Bug bug = bugMapper.selectByPrimaryKey(bugId);
        if (bug == null) {
            throw new MSException(BUG_NOT_EXIST);
        }
        return bug;
    }

    /**
     * 根据批量操作参数获取批量日志
     * @param batchIds 批量操作ID
     * @param operationType 操作类型
     * @param path 请求路径
     * @param batchUpdate 是否批量更新
     * @return 日志集合
     */
    private List<LogDTO> getBatchLogByRequest(List<String> batchIds, String operationType, String path, String projectId, boolean batchUpdate,
                                              boolean appendTag, List<String> modifiedTags, String currentUser) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        BugExample example = new BugExample();
        example.createCriteria().andIdIn(batchIds);
        List<Bug> bugs = bugMapper.selectByExample(example);
        List<LogDTO> logs = new ArrayList<>();
        bugs.forEach(bug -> {
            LogDTO log = new LogDTO(bug.getProjectId(), project.getOrganizationId(), bug.getId(), currentUser, operationType, OperationLogModule.BUG_MANAGEMENT_INDEX, bug.getTitle());
            log.setPath(path);
            log.setMethod(HttpMethodConstants.POST.name());
            if (batchUpdate) {
                // 批量更新只记录TAG的变更内容
                log.setOriginalValue(JSON.toJSONBytes(bug.getTags()));
                log.setModifiedValue(JSON.toJSONBytes(appendTag ? ListUtils.union(bug.getTags(), modifiedTags) : modifiedTags));
            } else {
                log.setOriginalValue(JSON.toJSONBytes(bug));
            }
            logs.add(log);
        });
        return logs;
    }

    /**
     * 获取下一个位置
     * @param projectId 项目ID
     * @return 位置
     */
    private Long getNextPos(String projectId) {
        Long pos = extBugMapper.getMaxPos(projectId);
        return (pos == null ? 0 : pos) + INTERVAL_POS;
    }

    /**
     * distinct by key
     * @param function distinct function
     * @return predicate
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> function) {
        Set<Object> keySet = ConcurrentHashMap.newKeySet();
        return t -> keySet.add(function.apply(t));
    }

    /**
     * 校验TAG长度
     * @param tags 标签集合
     */
    private void checkTagLength(List<String> tags) {
        if (CollectionUtils.isNotEmpty(tags) && tags.size() > MAX_TAG_SIZE) {
            throw new MSException(Translator.getWithArgs("bug_tags_size_large_than", String.valueOf(MAX_TAG_SIZE)));
        }
    }

    /**
     * 构建缺陷本地附件
     * @param bugId 缺陷ID
     * @param fileName 文件名称
     * @param size 文件大小
     * @param currentUser 当前用户
     * @return 本地附件
     */
    private BugLocalAttachment buildBugLocalAttachment(String bugId, String fileName, long size, String currentUser) {
        BugLocalAttachment bugAttachment = new BugLocalAttachment();
        bugAttachment.setId(IDGenerator.nextStr());
        bugAttachment.setBugId(bugId);
        bugAttachment.setFileId(IDGenerator.nextStr());
        bugAttachment.setFileName(fileName);
        bugAttachment.setSize(size);
        bugAttachment.setSource(BugAttachmentSourceType.ATTACHMENT.name());
        bugAttachment.setCreateTime(System.currentTimeMillis());
        bugAttachment.setCreateUser(currentUser);
        return bugAttachment;
    }

    /**
     * 获取当前项目下成员选项
     * @param projectId 项目ID
     * @return 选项集合
     */
    private List<CustomFieldOption> getMemberOption(String projectId) {
        List<SelectOption> localHandlerOption = bugCommonService.getLocalHandlerOption(projectId);
        return localHandlerOption.stream().map(user -> {
            CustomFieldOption option = new CustomFieldOption();
            option.setText(user.getText());
            option.setValue(user.getValue());
            return option;
        }).toList();
    }
}