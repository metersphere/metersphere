package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.request.CustomFieldOptionRequest;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.domain.CustomFieldOption;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateCustomField;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CreateTemplateResourceService implements CreateProjectResourceService {
    @Resource
    ProjectMapper projectMapper;
    @Resource
    BaseTemplateCustomFieldService baseTemplateCustomFieldService;
    @Resource
    BaseTemplateService baseTemplateService;
    @Resource
    BaseCustomFieldService baseCustomFieldService;
    @Resource
    BaseCustomFieldOptionService baseCustomFieldOptionService;

    @Override
    public void createResources(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        String organizationId = project.getOrganizationId();
        for (TemplateScene scene : TemplateScene.values()) {
            if (baseTemplateService.isOrganizationTemplateEnable(organizationId, scene.name())) {
                // 如果没有开启项目模板，则根据组织模板创建项目模板
                // 先创建字段再创建模板
                createProjectCustomField(projectId, organizationId, scene);
                createProjectTemplate(projectId, organizationId, scene);
            } else {
                // 开启了项目模板，则初始化项目模板和字段
                initProjectTemplate(projectId, scene);
            }
        }
    }

    /**
     * 当开启项目模板后，创建项目时初始化项目模板
     *
     * @param projectId
     * @param scene
     */
    private void initProjectTemplate(String projectId, TemplateScene scene) {
        switch (scene) {
            case FUNCTIONAL:
                baseTemplateService.initFunctionalDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case ISSUE:
                baseTemplateService.initIssueDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case API:
                baseTemplateService.initApiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case UI:
                baseTemplateService.initUiDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            case TEST_PLAN:
                baseTemplateService.initTestPlanDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                break;
            default:
                break;
        }
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织模板创建项目模板
     *
     * @param projectId
     * @param organizationId
     * @param scene
     */
    private void createProjectTemplate(String projectId, String organizationId, TemplateScene scene) {
        // 同步创建项目级别模板
        List<Template> orgTemplates = baseTemplateService.getTemplates(organizationId, scene.name());
        List<String> orgTemplateIds = orgTemplates.stream().map(Template::getId).toList();
        Map<String, List<TemplateCustomField>> customFieldMap = baseTemplateCustomFieldService.getByTemplateIds(orgTemplateIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getTemplateId()));

        orgTemplates.forEach((template) -> {
            List<TemplateCustomField> templateCustomFields = customFieldMap.get(template.getId());
            templateCustomFields = templateCustomFields == null ? List.of() : templateCustomFields;
            List<TemplateCustomFieldRequest> templateCustomFieldRequests = templateCustomFields.stream()
                    .map(templateCustomField -> BeanUtils.copyBean(new TemplateCustomFieldRequest(), templateCustomField))
                    .toList();
            addRefProjectTemplate(projectId, template, templateCustomFieldRequests);
        });
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织字段创建项目字段
     *
     * @param projectId
     * @param organizationId
     * @param scene
     */
    private void createProjectCustomField(String projectId, String organizationId, TemplateScene scene) {
        // 查询组织字段和选项
        List<CustomField> orgFields = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name());
        List<String> orgFieldIds = orgFields.stream().map(CustomField::getId).toList();
        Map<String, List<CustomFieldOption>> customFieldOptionMap = baseCustomFieldOptionService.getByFieldIds(orgFieldIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getFieldId()));

        orgFields.forEach((field) -> {
            List<CustomFieldOption> options = customFieldOptionMap.get(field.getId());
            List<CustomFieldOptionRequest> templateCustomFieldRequests = options == null ? List.of() : options.stream()
                    .map(option -> BeanUtils.copyBean(new CustomFieldOptionRequest(), option))
                    .toList();
            addRefProjectCustomField(projectId, field, templateCustomFieldRequests);
        });
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织字段创建项目字段
     *
     * @param orgCustomField
     * @param options
     */
    public void addRefProjectCustomField(String projectId, CustomField orgCustomField, List<CustomFieldOptionRequest> options) {
        CustomField customField = BeanUtils.copyBean(new CustomField(), orgCustomField);
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        customField.setScopeId(projectId);
        customField.setRefId(orgCustomField.getId());
        baseCustomFieldService.baseAdd(customField, options);
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织模板创建项目模板
     *
     * @param orgTemplate
     * @param customFields
     */
    public void addRefProjectTemplate(String projectId, Template orgTemplate, List<TemplateCustomFieldRequest> customFields) {
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        template.setScopeId(projectId);
        template.setRefId(orgTemplate.getId());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        List<TemplateCustomFieldRequest> refCustomFields = baseTemplateService.getRefTemplateCustomFieldRequest(projectId, customFields);
        baseTemplateService.baseAdd(template, refCustomFields);
    }
}
