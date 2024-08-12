package io.metersphere.system.service;

import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.OrganizationParameter;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateSystemCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationTemplateMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateService extends BaseTemplateService {

    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    protected ExtOrganizationTemplateMapper extOrganizationTemplateMapper;
    @Resource
    protected BaseOrganizationParameterService baseOrganizationParameterService;

    @Override
    public List<Template> list(String organizationId, String scene) {
        OrganizationService.checkResourceExist(organizationId);
        return super.list(organizationId, scene);
    }

    public TemplateDTO geDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkOrgResourceExist(template);
        TemplateDTO templateDTO = super.getTemplateDTO(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    public Template add(TemplateUpdateRequest request, String creator) {
        Template template = BeanUtils.copyBean(new Template(), request);
        template.setCreateUser(creator);
        checkOrgResourceExist(template);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        // 同步创建项目级别模板
        addRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    /**
     * 同步创建项目级别模板
     * 当开启组织模板时，操作组织模板，同时维护与之关联的项目模板
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgTemplate
     * @param customFields
     */
    public void addRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        String orgId = orgTemplate.getScopeId();
        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        projectIds.forEach(projectId -> {
            template.setScopeId(projectId);
            template.setRefId(orgTemplate.getId());
            template.setScopeType(TemplateScopeType.PROJECT.name());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectId, customFields);
            super.baseAdd(template, refCustomFields, systemFields);
        });
    }

    public void checkOrgResourceExist(Template template) {
        OrganizationService.checkResourceExist(template.getScopeId());
    }

    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkOrganizationTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkOrgResourceExist(originTemplate);
        updateRefProjectTemplate(template, request.getCustomFields(), request.getSystemFields());
        template.setRefId(null);
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    /**
     * 保存上传的文件
     * 将文件从临时目录移动到正式目录
     *
     * @param request
     */
    private void saveUploadImages(TemplateUpdateRequest request) {
        String orgTemplateDir = DefaultRepositoryDir.getOrgTemplateImgDir(request.getScopeId());
        String orgTemplatePreviewDir = DefaultRepositoryDir.getOrgTemplateImgPreviewDir(request.getScopeId());
        commonFileService.saveReviewImgFromTempFile(orgTemplateDir, orgTemplatePreviewDir, request.getUploadImgFileIds());
    }

    /**
     * 同步更新项目级别模板
     * 当开启组织模板时，操作组织模板，同时维护与之关联的项目模板
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgTemplate
     * @param customFields
     */
    public void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields, List<TemplateSystemCustomFieldRequest> systemFields) {
        List<Template> projectTemplates = getByRefId(orgTemplate.getId());
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        projectTemplates.forEach(projectTemplate -> {
            template.setId(projectTemplate.getId());
            template.setScopeId(projectTemplate.getScopeId());
            template.setRefId(orgTemplate.getId());
            template.setScene(orgTemplate.getScene());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(projectTemplate.getScopeId(), customFields);
            super.update(template, refCustomFields, systemFields);
        });
    }

    public List<Template> getByRefId(String refId) {
        TemplateExample example = new TemplateExample();
        example.createCriteria().andRefIdEqualTo(refId);
        return templateMapper.selectByExample(example);
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        deleteRefProjectTemplate(id);
        super.delete(id);
    }

    /**
     * 同步删除项目级别模板
     * 当开启组织模板时，操作组织模板，同时维护与之关联的项目模板
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param orgTemplateId
     */
    public void deleteRefProjectTemplate(String orgTemplateId) {
        // 删除关联的项目模板
        TemplateExample example = new TemplateExample();
        example.createCriteria().andRefIdEqualTo(orgTemplateId);
        templateMapper.deleteByExample(example);

        // 删除项目模板和字段的关联关系
        List<String> projectTemplateIds = extOrganizationTemplateMapper.getTemplateIdByRefId(orgTemplateId);
        // 分批删除
        SubListUtils.dealForSubList(projectTemplateIds, 100, baseTemplateCustomFieldService::deleteByTemplateIds);
    }

    public void checkOrganizationTemplateEnable(String orgId, String scene) {
        if (!isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(ORGANIZATION_TEMPLATE_PERMISSION);
        }
    }

    /**
     * 禁用组织模板，即启用项目模板
     * 启用后添加组织级别的系统参数
     * 启用后不可逆，只添加一次
     *
     * @param orgId
     * @param scene
     */
    public void disableOrganizationTemplate(String orgId, String scene) {
        if (StringUtils.isBlank(baseOrganizationParameterService.getValue(orgId, scene))) {
            OrganizationParameter organizationParameter = new OrganizationParameter();
            organizationParameter.setOrganizationId(orgId);
            organizationParameter.setParamKey(baseOrganizationParameterService.getOrgTemplateEnableKeyByScene(scene));
            organizationParameter.setParamValue(BooleanUtils.toStringTrueFalse(false));
            baseOrganizationParameterService.add(organizationParameter);
        }
    }

    /**
     * 一个接口返回各个模板是否启用组织模板
     *
     * @param organizationId
     * @return
     */
    public Map<String, Boolean> getOrganizationTemplateEnableConfig(String organizationId) {
        OrganizationService.checkResourceExist(organizationId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.FUNCTIONAL, TemplateScene.BUG)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), isOrganizationTemplateEnable(organizationId, scene.name())));
        return templateEnableConfig;
    }

    /**
     * 富文本框图片预览
     * @param organizationId
     * @param fileId
     * @param compressed
     * @return
     */
    public ResponseEntity<byte[]> previewImg(String organizationId, String fileId, boolean compressed) {
        String orgTemplateImgDir = DefaultRepositoryDir.getOrgTemplateImgDir(organizationId);
        String orgTemplateImgPreviewDir = DefaultRepositoryDir.getOrgTemplateImgPreviewDir(organizationId);
        return super.previewImg(fileId, orgTemplateImgDir, orgTemplateImgPreviewDir, compressed);
    }
}