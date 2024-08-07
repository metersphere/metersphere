package io.metersphere.system.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.request.TemplateCustomFieldRequest;
import io.metersphere.system.dto.sdk.request.TemplateSystemCustomFieldRequest;
import io.metersphere.system.resolver.field.AbstractCustomFieldResolver;
import io.metersphere.system.resolver.field.CustomFieldResolverFactory;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
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
    @Resource
    protected BaseStatusDefinitionService baseStatusDefinitionService;
    @Resource
    protected BaseStatusItemService baseStatusItemService;
    @Resource
    protected BaseStatusFlowService baseStatusFlowService;
    @Resource
    protected BaseStatusFlowSettingService baseStatusFlowSettingService;

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
                createProjectStatusSetting(projectId, organizationId, scene);
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
            case BUG:
                baseTemplateService.initBugDefaultTemplate(projectId, TemplateScopeType.PROJECT);
                baseStatusFlowSettingService.initBugDefaultStatusFlowSetting(projectId, TemplateScopeType.PROJECT);
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
        Map<String, List<TemplateCustomField>> templateCustomFieldMap = baseTemplateCustomFieldService.getByTemplateIds(orgTemplateIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getTemplateId()));

        Map<String, CustomField> customFieldMap = baseCustomFieldService.getByScopeIdAndScene(organizationId, scene.name()).stream()
                .collect(Collectors.toMap(CustomField::getId, Function.identity()));

        // 忽略默认值校验，可能有多选框的选项被删除，造成不合法数据
        BaseTemplateCustomFieldService.validateDefaultValue.set(false);

        orgTemplates.forEach((template) -> {
            List<TemplateCustomField> templateCustomFields = templateCustomFieldMap.get(template.getId());
            templateCustomFields = templateCustomFields == null ? List.of() : templateCustomFields;
            List<TemplateCustomFieldRequest> templateCustomFieldRequests = templateCustomFields.stream()
                    .map(templateCustomField -> {
                        TemplateCustomFieldRequest templateCustomFieldRequest = BeanUtils.copyBean(new TemplateCustomFieldRequest(), templateCustomField);
                        CustomField customField = customFieldMap.get(templateCustomField.getFieldId());
                        try {
                            if (StringUtils.isNotBlank(templateCustomField.getDefaultValue())) {
                                // 将字符串转成对应的对象，方便调用统一的创建方法
                                AbstractCustomFieldResolver customFieldResolver = CustomFieldResolverFactory.getResolver(customField.getType());
                                templateCustomFieldRequest.setDefaultValue(customFieldResolver.parse2Value(templateCustomField.getDefaultValue()));
                            }
                        } catch (Exception e) {
                            BaseTemplateCustomFieldService.validateDefaultValue.remove();
                            LogUtils.error(e);
                            templateCustomFieldRequest.setDefaultValue(null);
                        }
                        return templateCustomFieldRequest;
                    })
                    .toList();
            addRefProjectTemplate(projectId, template, templateCustomFieldRequests, null);
        });
        BaseTemplateCustomFieldService.validateDefaultValue.remove();
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
            addRefProjectCustomField(projectId, field, options);
        });
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织状态流创建项目状态流
     *
     * @param projectId
     * @param organizationId
     * @param scene
     */
    private void createProjectStatusSetting(String projectId, String organizationId, TemplateScene scene) {
        List<StatusItem> orgStatusItems = baseStatusItemService.getStatusItems(organizationId, scene.name());
        List<String> orgStatusItemIds = orgStatusItems.stream().map(StatusItem::getId).toList();

        // 同步创建项目级别状态项
        List<StatusItem> projectStatusItems = orgStatusItems.stream().map(orgStatusItem -> {
            StatusItem statusItem = BeanUtils.copyBean(new StatusItem(), orgStatusItem);
            statusItem.setScopeType(TemplateScopeType.PROJECT.name());
            statusItem.setRefId(orgStatusItem.getId());
            statusItem.setScopeId(projectId);
            statusItem.setId(UUID.randomUUID().toString());
            return statusItem;
        }).toList();
        baseStatusItemService.batchAdd(projectStatusItems);

        // 构建组织状态与对应项目状态的映射关系
        Map<String, String> statusRefMap = projectStatusItems.stream()
                .collect(Collectors.toMap(StatusItem::getRefId, StatusItem::getId));

        // 同步创建项目级别状态定义
        List<StatusDefinition> orgStatusDefinitions = baseStatusDefinitionService.getStatusDefinitions(orgStatusItemIds);
        List<StatusDefinition> projectStatusDefinition = orgStatusDefinitions.stream().map(orgStatusDefinition -> {
            StatusDefinition statusDefinition = BeanUtils.copyBean(new StatusDefinition(), orgStatusDefinition);
            statusDefinition.setStatusId(statusRefMap.get(orgStatusDefinition.getStatusId()));
            return statusDefinition;
        }).toList();
        baseStatusDefinitionService.batchAdd(projectStatusDefinition);

        // 同步创建项目级别状态流
        List<StatusFlow> orgStatusFlows = baseStatusFlowService.getStatusFlows(orgStatusItemIds);
        List<StatusFlow> projectStatusFlows = orgStatusFlows.stream().map(orgStatusFlow -> {
            StatusFlow statusFlow = BeanUtils.copyBean(new StatusFlow(), orgStatusFlow);
            statusFlow.setToId(statusRefMap.get(orgStatusFlow.getToId()));
            statusFlow.setFromId(statusRefMap.get(orgStatusFlow.getFromId()));
            statusFlow.setId(UUID.randomUUID().toString());
            return statusFlow;
        }).toList();
        baseStatusFlowService.batchAdd(projectStatusFlows);
    }

    /**
     * 当没有开启项目模板，创建项目时要根据当前组织字段创建项目字段
     *
     * @param orgCustomField
     * @param options
     */
    public void addRefProjectCustomField(String projectId, CustomField orgCustomField, List<CustomFieldOption> options) {
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
    public void addRefProjectTemplate(String projectId, Template orgTemplate, List<TemplateCustomFieldRequest> customFields,
                                      List<TemplateSystemCustomFieldRequest> systemCustomFields) {
        Template template = BeanUtils.copyBean(new Template(), orgTemplate);
        template.setScopeId(projectId);
        template.setRefId(orgTemplate.getId());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        List<TemplateCustomFieldRequest> refCustomFields = baseTemplateService.getRefTemplateCustomFieldRequest(projectId, customFields);
        baseTemplateService.baseAdd(template, refCustomFields, systemCustomFields);
    }
}
