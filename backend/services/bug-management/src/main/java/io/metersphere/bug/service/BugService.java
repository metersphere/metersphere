package io.metersphere.bug.service;

import io.metersphere.bug.domain.*;
import io.metersphere.bug.dto.BugCustomFieldDTO;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.BugRelateCaseCountDTO;
import io.metersphere.bug.dto.BugTagEditDTO;
import io.metersphere.bug.dto.request.BugBatchRequest;
import io.metersphere.bug.dto.request.BugBatchUpdateRequest;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.request.BugPageRequest;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.*;
import io.metersphere.bug.utils.CustomFieldUtils;
import io.metersphere.project.dto.filemanagement.FileLogRecord;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.FileAssociationSourceUtil;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Template;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.file.FileRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.bug.enums.result.BugResultCode.BUG_NOT_EXIST;

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
    private BugContentMapper bugContentMapper;
    @Resource
    private BaseUserMapper baseUserMapper;
    @Resource
    protected TemplateMapper templateMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ProjectTemplateService projectTemplateService;
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
    private ExtBugLocalAttachmentMapper extBugLocalAttachmentMapper;
    @Resource
    private FileService fileService;
    @Resource
    private BaseTemplateService baseTemplateService;
    @Resource
    private BugFollowerMapper bugFollowerMapper;

    public static final String ADD_BUG_FILE_LOG_URL = "/bug/add";
    public static final String UPDATE_BUG_FILE_LOG_URL = "/bug/update";
    public static final String UPLOAD_SOURCE_DIR = "/project";
    public static final String UPLOAD_APP_DIR = "/bug";

    /**
     * 缺陷列表查询
     *
     * @param request 列表请求参数
     * @return 缺陷列表
     */
    public List<BugDTO> list(BugPageRequest request) {
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<BugDTO> bugs = extBugMapper.list(request);
        if (CollectionUtils.isEmpty(bugs)) {
            return new ArrayList<>();
        }
        // 处理自定义字段及状态字段
        List<BugDTO> bugList = handleCustomFieldsAndStatus(bugs, request.getProjectId());
        return buildExtraInfo(bugList);
    }

    /**
     * 创建缺陷
     *
     * @param request 缺陷请求参数
     * @param files 附件集合
     * @param currentUser 当前用户
     */
    public void add(BugEditRequest request, List<MultipartFile> files, String currentUser) {
        /*
         *  缺陷创建逻辑
         *  1. 判断所属项目是否关联第三方平台;
         *  2. 根据模板自定义字段保存缺陷, 附件一起保存;
         *  3. 第三方平台需调用插件同步缺陷至其他平台(API字段, 附件需处理);
         *  4. 变更历史, 操作记录;
         */
        // TODO: 后续补充第三方平台同步逻辑(缺陷, 附件)
//        String platformName = projectApplicationService.getPlatformName(request.getProjectId());
//        request.setPlatform(platformName);
//        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
//            // 关联第三方平台
//            ServiceIntegration serviceIntegration = projectTemplateService.getServiceIntegration(request.getProjectId());
//            if (serviceIntegration == null) {
//                // 项目未配置第三方平台
//                throw new MSException(Translator.get("third_party_not_config"));
//            }
//            // 获取配置平台, 插入平台缺陷
//            Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
//                    new String(serviceIntegration.getConfiguration()));
//            PlatformBugUpdateRequest platformRequest = buildPlatformBugRequest(request);
//            String platformBugId = platform.addBug(platformRequest);
//            request.setPlatformBugId(platformBugId);
//        }
        // 缺陷基础字段
        handleAndSaveBug(request, currentUser, BugPlatform.LOCAL.getName());
        // 自定义字段
        handleAndSaveCustomFields(request, false, false);
        // 附件
        handleAndSaveAttachments(request, files, currentUser, ADD_BUG_FILE_LOG_URL);
    }

    /**
     * 更新缺陷
     *
     * @param request 缺陷请求参数
     * @param files 附件集合
     * @param currentUser 当前用户
     */
    public void update(BugEditRequest request, List<MultipartFile> files, String currentUser) {
        /*
         *  缺陷更新逻辑
         *  1. 保存逻辑与创建(1, 2, 3, 4)一致.
         *  2. 需校验状态流
         */
        // TODO: 后续补充第三方平台同步逻辑(缺陷, 附件)
        // 缺陷
        handleAndSaveBug(request, currentUser, BugPlatform.LOCAL.getName());
        // 自定义字段
        handleAndSaveCustomFields(request, true, false);
        // 附件
        handleAndSaveAttachments(request, files, currentUser, UPDATE_BUG_FILE_LOG_URL);
    }

    /**
     * 删除缺陷
     *
     * @param id 缺陷ID
     */
    public void delete(String id) {
        Bug bug = bugMapper.selectByPrimaryKey(id);
        if (bug == null) {
            throw new MSException(BUG_NOT_EXIST);
        }
        if (StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())) {
            Bug record = new Bug();
            record.setId(id);
            record.setDeleted(true);
            bugMapper.updateByPrimaryKeySelective(record);
        } else {
            bugMapper.deleteByPrimaryKey(id);
        }
    }

    /**
     * 获取缺陷模板详情
     *
     * @param templateId 模板ID
     * @param projectId 项目ID
     * @return 模板详情
     */
    public TemplateDTO getTemplate(String templateId, String projectId) {
        Template template = templateMapper.selectByPrimaryKey(templateId);
        if (template != null) {
            // 属于系统模板
            return baseTemplateService.getTemplateDTO(template);
        } else {
            // 不属于系统模板, TODO : 后续补充第三方平台模板
//            List<ProjectTemplateOptionDTO> option = projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
//            Optional<ProjectTemplateOptionDTO> isThirdPartyDefaultTemplate = option.stream().filter(projectTemplateOptionDTO -> StringUtils.equals(projectTemplateOptionDTO.getId(), templateId)).findFirst();
//            if (isThirdPartyDefaultTemplate.isPresent()) {
//                // TODO: 获取第三方平台模板
//                return null;
//            } else {
//                // 不属于系统模板&&不属于第三方平台默认模板, 则该模板已被删除
//                return projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.BUG.name());
//            }
            return projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.BUG.name());
        }
    }

    /**
     * 批量删除缺陷
     * @param request 请求参数
     */
    public void batchDelete(BugBatchRequest request) {
        // 非Local直接删除, Local移入回收站
        if (request.isSelectAll()) {
            // 全选
            BugPageRequest bugPageRequest = new BugPageRequest();
            BeanUtils.copyBean(bugPageRequest, request);
            bugPageRequest.setUseTrash(false);
            CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(bugPageRequest);
            List<BugDTO> bugs = extBugMapper.list(bugPageRequest);
            if (CollectionUtils.isNotEmpty(bugs)) {
                List<String> deleteIds = bugs.stream().filter(bug -> !StringUtils.equals(BugPlatform.LOCAL.getName(), bug.getPlatform())).map(BugDTO::getId).toList();
                if (CollectionUtils.isNotEmpty(deleteIds)) {
                    BugExample bugExample = new BugExample();
                    bugExample.createCriteria().andIdIn(deleteIds);
                    bugMapper.deleteByExample(bugExample);
                }
                bugs.stream().filter(bug -> StringUtils.equals(bug.getPlatform(), BugPlatform.LOCAL.getName())).forEach(bug -> {
                    Bug record = new Bug();
                    record.setId(bug.getId());
                    record.setDeleted(true);
                    bugMapper.updateByPrimaryKeySelective(record);
                });
            }
        } else {
            // 勾选部分
            if (CollectionUtils.isEmpty(request.getIncludeBugIds())) {
                throw new MSException(Translator.get("no_bug_select"));
            }
            // 勾选操作数据较少, 可逐条删除
            request.getIncludeBugIds().forEach(this::delete);
        }
    }

    /**
     * 批量编辑缺陷
     * @param request 请求参数
     */
    public void batchUpdate(BugBatchUpdateRequest request) {
        List<String> handleIds = new ArrayList<>();
        if (request.isSelectAll()) {
            // 全选
            BugPageRequest bugPageRequest = new BugPageRequest();
            BeanUtils.copyBean(bugPageRequest, request);
            bugPageRequest.setUseTrash(false);
            CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(bugPageRequest);
            List<BugDTO> bugs = extBugMapper.list(bugPageRequest);
            if (CollectionUtils.isNotEmpty(bugs)) {
                handleIds = bugs.stream().map(BugDTO::getId).toList();
            }
        } else {
            // 勾选部分
            if (CollectionUtils.isEmpty(request.getIncludeBugIds())) {
                throw new MSException(Translator.get("no_bug_select"));
            }
            handleIds = request.getIncludeBugIds();
        }
        if (CollectionUtils.isEmpty(handleIds)) {
            throw new MSException(Translator.get("no_bug_select"));
        }
        if (request.getCustomField() == null) {
            // 系统字段处理, TAG需单独处理追加的问题
            if (StringUtils.isNotEmpty(request.getTag()) && request.isAppend()) {
                // 标签(追加)
                List<BugTagEditDTO> bugTagList = extBugMapper.getBugTagList(handleIds);
                if (CollectionUtils.isEmpty(bugTagList)) {
                    throw new MSException(Translator.get("bug_select_not_found"));
                }
                Map<String, String> bugTagMap = bugTagList.stream().collect(Collectors.toMap(BugTagEditDTO::getBugId, b -> Optional.ofNullable(b.getTag()).orElse(StringUtils.EMPTY)));
                SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                BugMapper batchMapper = sqlSession.getMapper(BugMapper.class);
                bugTagMap.forEach((k, v) -> {
                    Bug record = new Bug();
                    record.setId(k);
                    record.setTag(CustomFieldUtils.appendToMultipleCustomField(v, request.getTag()));
                    batchMapper.updateByPrimaryKeySelective(record);
                });
                sqlSession.flushStatements();
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            } else  {
                extBugMapper.batchUpdate(request, handleIds);
            }
        } else {
            // 自定义字段处理 (第三方模板字段暂时不支持批量编辑, 涉及到同步)
            BugCustomFieldDTO customField = request.getCustomField();
            Map<String, String> customFieldMap = new HashMap<>(1);
            customFieldMap.put(customField.getId(), customField.getValue());
            handleIds.forEach(id -> {
                BugEditRequest bugEditRequest = new BugEditRequest();
                BeanUtils.copyBean(bugEditRequest, request);
                bugEditRequest.setId(id);
                bugEditRequest.setCustomFieldMap(customFieldMap);
                handleAndSaveCustomFields(bugEditRequest, true, request.isAppend());
            });
        }
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
     * 处理保存缺陷基础信息
     *
     * @param request 请求参数
     * @param currentUser 当前用户ID
     * @param platformName 第三方平台名称
     */
    private void handleAndSaveBug(BugEditRequest request, String currentUser, String platformName) {
        Bug bug = new Bug();
        BeanUtils.copyBean(bug, request);
        bug.setPlatform(platformName);
        // TODO: 关于平台, 后续补充, 暂保留
//        if (StringUtils.equalsIgnoreCase(BugPlatform.LOCAL.getName(), platformName)) {
//            bug.setPlatformBugId(null);
//        } else {
//            bug.setPlatformBugId(platformBugId);
//        }
        if (StringUtils.isEmpty(bug.getId())) {
            bug.setId(IDGenerator.nextStr());
            bug.setNum(Long.valueOf(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.BUG_MANAGEMENT)).intValue());
            bug.setHandleUsers(request.getHandleUser());
            bug.setCreateUser(currentUser);
            bug.setCreateTime(System.currentTimeMillis());
            bug.setUpdateUser(currentUser);
            bug.setUpdateTime(System.currentTimeMillis());
            bug.setDeleteUser(currentUser);
            bug.setDeleteTime(System.currentTimeMillis());
            bug.setDeleted(false);
            bugMapper.insert(bug);
            request.setId(bug.getId());
            BugContent bugContent = new BugContent();
            bugContent.setBugId(bug.getId());
            bugContent.setDescription(request.getDescription());
            bugContentMapper.insert(bugContent);
        } else {
            Bug orignalBug = checkBugExist(request.getId());
            // 追加处理人
            bug.setHandleUsers(orignalBug.getHandleUsers() + "," + request.getHandleUser());
            bug.setUpdateUser(currentUser);
            bug.setUpdateTime(System.currentTimeMillis());
            bugMapper.updateByPrimaryKeySelective(bug);
            BugContent bugContent = new BugContent();
            bugContent.setBugId(bug.getId());
            bugContent.setDescription(request.getDescription());
            bugContentMapper.updateByPrimaryKeySelective(bugContent);
        }
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
     * 处理保存自定义字段信息
     *
     * @param request 请求参数
     */
    private void handleAndSaveCustomFields(BugEditRequest request,  boolean merge, boolean append) {
        Map<String, String> customFieldMap = request.getCustomFieldMap();
        if (MapUtils.isEmpty(customFieldMap)) {
            return;
        }
        List<BugCustomField> addFields = new ArrayList<>();
        List<BugCustomField> updateFields = new ArrayList<>();
        if (merge) {
            // 编辑缺陷需合并原有自定义字段
            List<BugCustomFieldDTO> originalFields = extBugCustomFieldMapper.getBugCustomFields(List.of(request.getId()), request.getProjectId());
            Map<String, String> originalFieldMap = originalFields.stream().collect(Collectors.toMap(BugCustomFieldDTO::getId, BugCustomFieldDTO::getValue));
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
                    if (append) {
                        // 追加处理只存在多选类型的自定义字段
                        bugCustomField.setValue(CustomFieldUtils.appendToMultipleCustomField(originalFieldMap.get(fieldId), customFieldMap.get(fieldId)));
                    } else {
                        bugCustomField.setValue(customFieldMap.get(fieldId));
                    }
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
    private void handleAndSaveAttachments(BugEditRequest request, List<MultipartFile> files, String currentUser, String fileLogUrl) {
        /*
         * 附件处理逻辑
         * 1. 先处理删除, 及取消关联的附件
         * 2. 再处理新上传的, 新关联的附件
         */
        if (CollectionUtils.isNotEmpty(request.getDeleteLocalFileIds())) {
            // 删除本地上传的附件, BUG_LOCAL_ATTACHMENT表
            request.getDeleteLocalFileIds().forEach(deleteFileId -> {
                FileRequest fileRequest = buildBugFileRequest(request.getProjectId(), deleteFileId, null);
                try {
                    fileService.deleteFile(fileRequest);
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_delete_error"));
                }
            });
            BugLocalAttachmentExample example = new BugLocalAttachmentExample();
            example.createCriteria().andBugIdEqualTo(request.getId()).andFileIdIn(request.getDeleteLocalFileIds());
            bugLocalAttachmentMapper.deleteByExample(example);
        }
        if (CollectionUtils.isNotEmpty(request.getUnLinkRefIds())) {
            // 取消关联的附件, FILE_ASSOCIATION表
            fileAssociationService.deleteBySourceId(request.getUnLinkRefIds(), createFileLogRecord(fileLogUrl, currentUser, request.getProjectId()));
        }

        // 新本地上传的附件
        List<BugLocalAttachment> addFiles = new ArrayList<>();
        Map<String, MultipartFile> uploadMinioFiles = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                BugLocalAttachment bugAttachment = new BugLocalAttachment();
                bugAttachment.setId(IDGenerator.nextStr());
                bugAttachment.setBugId(request.getId());
                bugAttachment.setFileId(IDGenerator.nextStr());
                bugAttachment.setFileName(file.getOriginalFilename());
                bugAttachment.setSize(file.getSize());
                bugAttachment.setCreateTime(System.currentTimeMillis());
                bugAttachment.setCreateUser(currentUser);
                addFiles.add(bugAttachment);
                uploadMinioFiles.put(bugAttachment.getFileId(), file);
            });
            extBugLocalAttachmentMapper.batchInsert(addFiles);
            uploadMinioFiles.forEach((fileId, file) -> {
                FileRequest fileRequest = buildBugFileRequest(request.getProjectId(), fileId, file.getOriginalFilename());
                try {
                    fileService.upload(file, fileRequest);
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_upload_error"));
                }
            });
        }
        // 新关联的附件
        if (CollectionUtils.isNotEmpty(request.getLinkFileIds())) {
            fileAssociationService.association(request.getId(), FileAssociationSourceUtil.SOURCE_TYPE_BUG, request.getLinkFileIds(), false,
                    createFileLogRecord(fileLogUrl, currentUser, request.getProjectId()));
        }
    }

//    /**
//     * 封装缺陷平台请求参数
//     * @param request 缺陷请求参数
//     */
//    private PlatformBugUpdateRequest buildPlatformBugRequest(BugEditRequest request) {
//        /*
//         * TODO: 封装缺陷平台请求参数
//         * 如果是系统默认模板, 只需获取模板中有配置API的字段并处理
//         * 如果是平台默认模板, 则处理参数中全部自定义字段
//         */
//        return new PlatformBugUpdateRequest();
//    }

    /**
     * 封装缺陷其他字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    private List<BugDTO> buildExtraInfo(List<BugDTO> bugs) {
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
            bug.setRelationCaseCount(countMap.get(bug.getId()));
            bug.setCreateUserName(userMap.get(bug.getCreateUser()));
            bug.setUpdateUser(userMap.get(bug.getUpdateUser()));
            bug.setDeleteUser(userMap.get(bug.getDeleteUser()));
            bug.setHandleUserName(userMap.get(bug.getHandleUser()));
        });
        return bugs;
    }

    /**
     * 处理自定义字段及状态字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    private List<BugDTO> handleCustomFieldsAndStatus(List<BugDTO> bugs, String projectId) {
        List<String> ids = bugs.stream().map(BugDTO::getId).toList();
        List<BugCustomFieldDTO> customFields = extBugCustomFieldMapper.getBugCustomFields(ids, projectId);
        Map<String, List<BugCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(BugCustomFieldDTO::getBugId));
        bugs.forEach(bug -> {
            bug.setCustomFields(customFieldMap.get(bug.getId()));
            // 缺陷状态选项值不存在时不展示 暂时保留, 项目插件同步配置开发后补充
//            List<BugStatusOptionDTO> statusOption = bugStatusService.getProjectStatusOption(bug.getProjectId());
//            if (CollectionUtils.isNotEmpty(statusOption)) {
//                Optional<BugStatusOptionDTO> statusOpt = statusOption.stream().filter(optionDTO -> optionDTO.getId().equals(bug.getStatus())).findFirst();
//                statusOpt.ifPresent(optionDTO -> bug.setStatusName(optionDTO.getName()));
//            }
        });
        return bugs;
    }

//    /**
//     * 获取第三方平台模板
//     *
//     * @param projectId 项目ID
//     * @return 第三方平台模板
//     */
//    private TemplateDTO getPluginBugTemplate(String projectId) {
//        ServiceIntegration serviceIntegration = projectTemplateService.getServiceIntegration(projectId);
//        if (serviceIntegration == null) {
//            return null;
//        }
//        TemplateDTO template = new TemplateDTO();
//        Template pluginTemplate = projectTemplateService.getPluginBugTemplate(projectId);
//        BeanUtils.copyBean(template, pluginTemplate);
//        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
//                new String(serviceIntegration.getConfiguration()));
//        // TODO: 插件平台获取自定义字段, 并针对特殊字段单独处理
//        List<PlatformCustomFieldItemDTO> platformCustomFields = platform.getThirdPartCustomField("");
//        if (CollectionUtils.isNotEmpty(platformCustomFields)) {
//            List<TemplateCustomFieldDTO> customFields = platformCustomFields.stream().map(platformCustomField -> {
//                TemplateCustomFieldDTO customField = new TemplateCustomFieldDTO();
//                customField.setFieldId(platformCustomField.getId());
//                customField.setFieldName(platformCustomField.getName());
//                customField.setDefaultValue(platformCustomField.getDefaultValue());
//                customField.setRequired(platformCustomField.getRequired());
//                return customField;
//            }).toList();
//            template.setCustomFields(customFields);
//        }
//        return template;
//    }

    private FileLogRecord createFileLogRecord(String logUrl, String operator, String projectId){
        return FileLogRecord.builder()
                .logModule(OperationLogModule.BUG_MANAGEMENT)
                .requestMethod(HttpMethodConstants.POST.name())
                .requestUrl(logUrl)
                .operator(operator)
                .projectId(projectId)
                .build();
    }

    private FileRequest buildBugFileRequest(String projectId, String resourceId, String fileName) {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(UPLOAD_SOURCE_DIR + "/" + projectId + UPLOAD_APP_DIR + "/" + resourceId);
        fileRequest.setProjectId(projectId);
        fileRequest.setResourceId(resourceId);
        fileRequest.setFileName(StringUtils.isEmpty(fileName) ? null : fileName);
        fileRequest.setStorage(StorageType.MINIO.name());
        return fileRequest;
    }
}
