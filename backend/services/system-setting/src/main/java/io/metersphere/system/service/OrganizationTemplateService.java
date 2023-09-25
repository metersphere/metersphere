package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.SubListUtil;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.OrganizationParameter;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.mapper.BaseProjectMapper;
import io.metersphere.system.mapper.ExtOrganizationTemplateMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.ORGANIZATION_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationTemplateService extends BaseTemplateService {

    @Resource
    private OrganizationService organizationService;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private BaseCustomFieldService baseCustomFieldService;
    @Resource
    protected ExtOrganizationTemplateMapper extOrganizationTemplateMapper;
    @Resource
    protected BaseOrganizationParameterService baseOrganizationParameterService;

    @Override
    public List<Template> list(String organizationId, String scene) {
        organizationService.checkResourceExist(organizationId);
        return super.list(organizationId, scene);
    }

    public TemplateDTO geDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkOrgResourceExist(template);
        return super.geDTOWithCheck(template);
    }

    @Override
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields) {
        checkOrgResourceExist(template);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.ORGANIZATION.name());
        template.setRefId(null);
        template = super.add(template, customFields);
        // 同步创建项目级别模板
        addRefProjectTemplate(template, customFields);
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
    public void addRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields) {
        String orgId = orgTemplate.getScopeId();
        List<String> projectIds = baseProjectMapper.getProjectIdByOrgId(orgId);
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        projectIds.forEach(projectId -> {
            template.setScopeId(projectId);
            template.setRefId(orgTemplate.getId());
            template.setScopeType(TemplateScopeType.PROJECT.name());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(customFields);
            super.add(template, refCustomFields);
        });
    }

    /**
     * 将创建组织模板与字段的关联关系请求
     * 转换为项目模板与字段的关联关系请求
     *
     * @param customFields
     * @return
     */
    private List<TemplateCustomFieldRequest> getRefTemplateCustomFieldRequest(List<TemplateCustomFieldRequest> customFields) {
        if (customFields == null) {
            return null;
        }
        List<String> fieldIds = customFields.stream()
                .map(TemplateCustomFieldRequest::getFieldId).toList();
        // 查询当前组织字段所对应的项目字段，构建map，键为组织字段ID，值为项目字段ID
        Map<String, String> refFieldMap = baseCustomFieldService.getByRefIds(fieldIds)
                .stream()
                .collect(Collectors.toMap(CustomField::getRefId, CustomField::getId));
        // 根据组织字段ID，替换为项目字段ID
        return customFields.stream()
                .map(item -> {
                    TemplateCustomFieldRequest request = BeanUtils.copyBean(new TemplateCustomFieldRequest(), item);
                    request.setFieldId(refFieldMap.get(item.getFieldId()));
                    return request;
                })
                .toList();
    }

    public void checkOrgResourceExist(Template template) {
        organizationService.checkResourceExist(template.getScopeId());
    }

    @Override
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields) {
        Template originTemplate = super.getWithCheck(template.getId());
        checkOrganizationTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        checkOrgResourceExist(originTemplate);
        updateRefProjectTemplate(template, customFields);
        template.setRefId(null);
        return super.update(template, customFields);
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
    public void updateRefProjectTemplate(Template orgTemplate, List<TemplateCustomFieldRequest> customFields) {
        List<Template> projectTemplates = getByRefId(orgTemplate.getId());
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        projectTemplates.forEach(projectTemplate -> {
            template.setId(projectTemplate.getId());
            template.setScopeId(projectTemplate.getScopeId());
            template.setRefId(orgTemplate.getId());
            List<TemplateCustomFieldRequest> refCustomFields = getRefTemplateCustomFieldRequest(customFields);
            super.update(template, refCustomFields);
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
     * 将模板设置成默认模板
     * 同时将其他没模板设置成非默认模板
     *
     * @param id
     */
    @Override
    public void setDefaultTemplate(String id) {
        Template template = getWithCheck(id);
        checkOrganizationTemplateEnable(template.getScopeId(), template.getScene());
        setRefDefaultTemplate(id);
        super.setDefaultTemplate(id);
    }

    /**
     * 同步设置项目级别模板
     * 当开启组织模板时，操作组织模板，同时维护与之关联的项目模板
     * 避免当开启项目模板时，需要将各个资源关联的模板和字段从组织切换为项目
     * 无论是否开启组织模板，各资源都只关联各自项目下的模板和字段
     *
     * @param id
     */
    private void setRefDefaultTemplate(String id) {
        getByRefId(id).stream()
                .map(Template::getId)
                .forEach(super::setDefaultTemplate);
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
        SubListUtil.dealForSubList(projectTemplateIds, 100, baseTemplateCustomFieldService::deleteByTemplateIds);
    }

    private void checkOrganizationTemplateEnable(String orgId, String scene) {
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
}