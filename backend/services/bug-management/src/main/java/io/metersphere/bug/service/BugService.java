package io.metersphere.bug.service;

import io.metersphere.bug.domain.*;
import io.metersphere.bug.dto.BugCustomFieldDTO;
import io.metersphere.bug.dto.BugDTO;
import io.metersphere.bug.dto.BugRelationCaseCountDTO;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.dto.request.BugRequest;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.*;
import io.metersphere.bug.utils.CustomFieldUtils;
import io.metersphere.plugin.platform.dto.PlatformBugUpdateRequest;
import io.metersphere.plugin.platform.dto.PlatformCustomFieldItemDTO;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.domain.FileMetadata;
import io.metersphere.project.domain.FileMetadataExample;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.project.mapper.FileMetadataMapper;
import io.metersphere.project.service.FileService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.TemplateCustomFieldDTO;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.Template;
import io.metersphere.system.mapper.BaseUserMapper;
import io.metersphere.system.mapper.TemplateMapper;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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

    public static final Integer DEFAULT_BUG_NUM = 100000;

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
    private PlatformPluginService platformPluginService;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    BugCustomFieldMapper bugCustomFieldMapper;
    @Resource
    private ExtBugCustomFieldMapper extBugCustomFieldMapper;
    @Resource
    private ExtBugRelationCaseMapper extBugRelationCaseMapper;
    @Resource
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private BugAttachmentMapper bugAttachmentMapper;
    @Resource
    private ExtBugAttachmentMapper extBugAttachmentMapper;
    @Resource
    private FileService fileService;

    /**
     * 缺陷列表查询
     *
     * @param request 列表请求参数
     * @return 缺陷列表
     */
    public List<BugDTO> list(BugRequest request) {
        CustomFieldUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<BugDTO> bugs = extBugMapper.list(request);
        if (CollectionUtils.isEmpty(bugs)) {
            return new ArrayList<>();
        }
        // 处理自定义字段及状态字段
        List<BugDTO> bugList = handleCustomFieldsAndStatus(bugs);
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
         * TODO: 缺陷创建逻辑(根据设计图调整)
         *  1. 判断所属项目是否关联第三方平台;
         *  2. 根据模板自定义字段保存缺陷, 附件一起保存;
         *  3. 第三方平台需调用插件同步缺陷至其他平台(API字段, 附件需处理);
         *  4. 变更历史, 操作记录;
         */
        String platformName = getPlatform(request.getProjectId());
        request.setPlatform(platformName);
        if (!StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            // 关联第三方平台
            ServiceIntegration serviceIntegration = projectTemplateService.getServiceIntegration(request.getProjectId());
            if (serviceIntegration == null) {
                // 项目未配置第三方平台
                throw new MSException(Translator.get("third_party_not_config"));
            }
            // 获取配置平台, 插入平台缺陷
            Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                    new String(serviceIntegration.getConfiguration()));
            PlatformBugUpdateRequest platformRequest = buildPlatformBugRequest(request);
            String platformBugId = platform.addBug(platformRequest);
            request.setPlatformBugId(platformBugId);
        }
        // 缺陷基础字段
        handleAndSaveBug(request, currentUser);
        // 自定义字段
        handleAndSaveCustomFields(request, false);
        // 附件
        handleAndSaveAttachments(request, files, currentUser);
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
         * TODO: 缺陷更新逻辑(根据设计图调整)
         *  1. 保存逻辑与创建(1, 2, 3)一致.
         *  2. 需校验状态流
         */
        // 缺陷
        handleAndSaveBug(request, currentUser);
        // 自定义字段
        handleAndSaveCustomFields(request, true);
        // 附件
        handleAndSaveAttachments(request, files, currentUser);
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
            bug.setTrash(true);
            bugMapper.updateByPrimaryKeySelective(bug);
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
            return null;
        } else {
            // 不属于系统模板
            List<ProjectTemplateOptionDTO> option = projectTemplateService.getOption(projectId, TemplateScene.BUG.name());
            Optional<ProjectTemplateOptionDTO> isThirdPartyDefaultTemplate = option.stream().filter(projectTemplateOptionDTO -> StringUtils.equals(projectTemplateOptionDTO.getId(), templateId)).findFirst();
            if (isThirdPartyDefaultTemplate.isPresent()) {
                // 第三方平台模板
                return getPluginBugTemplate(projectId);
            } else {
                // 不属于系统模板&&不属于第三方平台默认模板, 则该模板已被删除
                return projectTemplateService.getDefaultTemplateDTO(projectId, TemplateScene.BUG.name());
            }
        }
    }

    /**
     * 处理保存缺陷基础信息
     *
     * @param request 请求参数
     * @param currentUser 当前用户ID
     */
    private void handleAndSaveBug(BugEditRequest request, String currentUser) {
        Bug bug = new Bug();
        BeanUtils.copyBean(bug, request);
        if (StringUtils.isEmpty(bug.getId())) {
            bug.setId(IDGenerator.nextStr());
            Long maxNum = extBugMapper.getMaxNum(request.getProjectId());
            bug.setNum(maxNum == null ? DEFAULT_BUG_NUM : maxNum.intValue() + 1);
            bug.setCreateUser(currentUser);
            bug.setCreateTime(System.currentTimeMillis());
            bug.setUpdateUser(currentUser);
            bug.setUpdateTime(System.currentTimeMillis());
            bug.setTrash(false);
            bugMapper.insert(bug);
            request.setId(bug.getId());
            BugContent bugContent = new BugContent();
            bugContent.setBugId(bug.getId());
            bugContent.setDescription(request.getDescription());
            bugContentMapper.insert(bugContent);
        } else {
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
     * 处理保存自定义字段信息
     *
     * @param request 请求参数
     */
    private void handleAndSaveCustomFields(BugEditRequest request,  boolean merge) {
        Map<String, Object> customFieldMap = request.getCustomFieldMap();
        if (customFieldMap.isEmpty()) {
            return;
        }
        List<BugCustomField> addFields = new ArrayList<>();
        List<BugCustomField> updateFields = new ArrayList<>();
        if (merge) {
            // 编辑缺陷需合并原有自定义字段
            List<BugCustomFieldDTO> originalFields = extBugCustomFieldMapper.getBugCustomFields(List.of(request.getId()));
            Map<String, String> originalFieldMap = originalFields.stream().collect(Collectors.toMap(BugCustomFieldDTO::getId, BugCustomFieldDTO::getValue));
            customFieldMap.keySet().forEach(fieldId -> {
                BugCustomField bugCustomField = new BugCustomField();
                if (!originalFieldMap.containsKey(fieldId)) {
                    // 新的缺陷字段关系
                    bugCustomField.setBugId(request.getId());
                    bugCustomField.setFieldId(fieldId);
                    bugCustomField.setValue(customFieldMap.get(fieldId).toString());
                    addFields.add(bugCustomField);
                } else {
                    // 已存在的缺陷字段关系
                    bugCustomField.setBugId(request.getId());
                    bugCustomField.setFieldId(fieldId);
                    bugCustomField.setValue(customFieldMap.get(fieldId).toString());
                    updateFields.add(bugCustomField);
                }
            });
        } else {
            // 新增缺陷不需要合并自定义字段
            customFieldMap.keySet().forEach(fieldId -> {
                BugCustomField bugCustomField = new BugCustomField();
                bugCustomField.setBugId(request.getId());
                bugCustomField.setFieldId(fieldId);
                bugCustomField.setValue(customFieldMap.get(fieldId).toString());
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
                bugCustomFieldMapper.updateByPrimaryKey(bugCustomField);
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
    private void handleAndSaveAttachments(BugEditRequest request, List<MultipartFile> files, String currentUser) {
        Map<String, MultipartFile> uploadMinioFiles = new HashMap<>(16);
        List<BugAttachment> addFiles = new ArrayList<>();
        // 处理删除的本地上传附件及取消关联的附件
        List<String> deleteIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(request.getDeleteLocalFileIds())) {
            deleteIds.addAll(request.getDeleteLocalFileIds());
            request.getDeleteLocalFileIds().forEach(deleteFileId -> {
                FileRequest fileRequest = new FileRequest();
                fileRequest.setProjectId(request.getProjectId());
                fileRequest.setFileName(deleteFileId);
                fileRequest.setStorage(StorageType.MINIO.name());
                try {
                    fileService.deleteFile(fileRequest);
                } catch (Exception e) {
                    throw new MSException(Translator.get("bug_attachment_delete_error"));
                }
            });
        }
        if (CollectionUtils.isNotEmpty(request.getUnLinkFileIds())) {
            deleteIds.addAll(request.getUnLinkFileIds());
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            BugAttachmentExample example = new BugAttachmentExample();
            example.createCriteria().andBugIdEqualTo(request.getId()).andFileIdIn(deleteIds);
            bugAttachmentMapper.deleteByExample(example);
            // TODO: 如果是第三方平台, 需调用平台插件同步删除附件
        }
        // 新本地上传的附件
        if (CollectionUtils.isNotEmpty(files)) {
            files.forEach(file -> {
                BugAttachment bugAttachment = new BugAttachment();
                bugAttachment.setId(IDGenerator.nextStr());
                bugAttachment.setBugId(request.getId());
                bugAttachment.setFileId(IDGenerator.nextStr());
                bugAttachment.setFileName(file.getName());
                bugAttachment.setSize(file.getSize());
                bugAttachment.setLocal(true);
                bugAttachment.setCreateTime(System.currentTimeMillis());
                bugAttachment.setCreateUser(currentUser);
                addFiles.add(bugAttachment);
                uploadMinioFiles.put(bugAttachment.getFileId(), file);
            });
        }
        // 新关联的附件
        List<String> linkIds = request.getLinkFileIds();
        if (CollectionUtils.isNotEmpty(linkIds)) {
            FileMetadataExample example = new FileMetadataExample();
            example.createCriteria().andIdIn(linkIds);
            List<FileMetadata> linkFiles = fileMetadataMapper.selectByExample(example);
            Map<String, FileMetadata> linkFileMap = linkFiles.stream().collect(Collectors.toMap(FileMetadata::getId, v -> v));
            linkIds.forEach(fileId -> {
                FileMetadata fileMetadata = linkFileMap.get(fileId);
                if (fileMetadata == null) {
                    return;
                }
                BugAttachment bugAttachment = new BugAttachment();
                bugAttachment.setId(IDGenerator.nextStr());
                bugAttachment.setBugId(request.getId());
                bugAttachment.setFileId(fileId);
                bugAttachment.setFileName(fileMetadata.getName());
                bugAttachment.setSize(fileMetadata.getSize());
                bugAttachment.setLocal(false);
                bugAttachment.setCreateTime(System.currentTimeMillis());
                bugAttachment.setCreateUser(currentUser);
                addFiles.add(bugAttachment);
            });
        }
        extBugAttachmentMapper.batchInsert(addFiles);
        // TODO: 如果是第三方平台, 需调用平台插件同步上传附件
        uploadMinioFiles.forEach((fileId, file) -> {
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(file.getName());
            fileRequest.setProjectId(request.getProjectId());
            fileRequest.setResourceId(MsFileUtils.ISSUE_ATTACHMENT_DIR + "/" + fileId);
            fileRequest.setStorage(StorageType.MINIO.name());
            try {
                fileService.upload(file, fileRequest);
            } catch (Exception e) {
                throw new MSException(Translator.get("bug_attachment_upload_error"));
            }
        });
    }

    /**
     * 封装缺陷平台请求参数
     * @param request 缺陷请求参数
     */
    private PlatformBugUpdateRequest buildPlatformBugRequest(BugEditRequest request) {
        /*
         * TODO: 封装缺陷平台请求参数
         * 如果是系统默认模板, 只需获取模板中有配置API的字段并处理
         * 如果是平台默认模板, 则处理参数中全部自定义字段
         */
        return new PlatformBugUpdateRequest();
    }

    /**
     * 封装缺陷其他字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    private List<BugDTO> buildExtraInfo(List<BugDTO> bugs) {
        // 获取用户集合
        List<String> userIds = new ArrayList<>();
        userIds.addAll(bugs.stream().map(BugDTO::getCreateUser).toList());
        userIds.addAll(bugs.stream().map(BugDTO::getAssignUser).toList());
        List<OptionDTO> userOptions = baseUserMapper.selectUserOptionByIds(userIds);
        Map<String, String> userMap = userOptions.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
        // 根据缺陷ID获取关联用例数
        List<String> ids = bugs.stream().map(BugDTO::getId).toList();
        List<BugRelationCaseCountDTO> relationCaseCount = extBugRelationCaseMapper.countRelationCases(ids);
        Map<String, Integer> countMap = relationCaseCount.stream().collect(Collectors.toMap(BugRelationCaseCountDTO::getBugId, BugRelationCaseCountDTO::getRelationCaseCount));
        bugs.forEach(bug -> {
            bug.setRelationCaseCount(countMap.get(bug.getId()));
            bug.setCreateUserName(userMap.get(bug.getCreateUser()));
            bug.setAssignUserName(userMap.get(bug.getAssignUser()));
        });
        return bugs;
    }

    /**
     * 处理自定义字段及状态字段
     *
     * @param bugs 缺陷集合
     * @return 缺陷DTO集合
     */
    private List<BugDTO> handleCustomFieldsAndStatus(List<BugDTO> bugs) {
        List<String> ids = bugs.stream().map(BugDTO::getId).toList();
        List<BugCustomFieldDTO> customFields = extBugCustomFieldMapper.getBugCustomFields(ids);
        Map<String, List<BugCustomFieldDTO>> customFieldMap = customFields.stream().collect(Collectors.groupingBy(BugCustomFieldDTO::getBugId));
        bugs.forEach(bug -> {
            bug.setCustomFields(customFieldMap.get(bug.getId()));
            // 缺陷状态选项值不存在时不展示
            List<OptionDTO> statusOption = CustomFieldUtils.getProjectStatusOption(bug.getProjectId());
            if (CollectionUtils.isNotEmpty(statusOption)) {
                Optional<OptionDTO> statusOpt = statusOption.stream().filter(optionDTO -> optionDTO.getId().equals(bug.getStatus())).findFirst();
                statusOpt.ifPresent(optionDTO -> bug.setStatusName(optionDTO.getName()));
            }
        });
        return bugs;
    }

    /**
     * 获取项目所属平台
     * @param projectId 项目ID
     * @return 项目所属平台
     */
    private String getPlatform(String projectId) {
        // TODO 获取项目所属平台
        return null;
    }

    /**
     * 获取第三方平台模板
     *
     * @param projectId 项目ID
     * @return 第三方平台模板
     */
    private TemplateDTO getPluginBugTemplate(String projectId) {
        ServiceIntegration serviceIntegration = projectTemplateService.getServiceIntegration(projectId);
        if (serviceIntegration == null) {
            return null;
        }
        TemplateDTO template = new TemplateDTO();
        Template pluginTemplate = projectTemplateService.getPluginBugTemplate(projectId);
        BeanUtils.copyBean(template, pluginTemplate);
        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
                new String(serviceIntegration.getConfiguration()));
        // TODO: 插件平台获取自定义字段, 并针对特殊字段单独处理
        List<PlatformCustomFieldItemDTO> platformCustomFields = platform.getThirdPartCustomField("");
        if (CollectionUtils.isNotEmpty(platformCustomFields)) {
            List<TemplateCustomFieldDTO> customFields = platformCustomFields.stream().map(platformCustomField -> {
                TemplateCustomFieldDTO customField = new TemplateCustomFieldDTO();
                customField.setFieldId(platformCustomField.getId());
                customField.setFieldName(platformCustomField.getName());
                customField.setDefaultValue(platformCustomField.getDefaultValue());
                customField.setRequired(platformCustomField.getRequired());
                return customField;
            }).toList();
            template.setCustomFields(customFields);
        }
        return template;
    }
}
