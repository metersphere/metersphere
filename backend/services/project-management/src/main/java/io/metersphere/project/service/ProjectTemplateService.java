package io.metersphere.project.service;

import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.ProjectTemplateDTO;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.sdk.constants.InternalUser;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.service.ServiceIntegrationService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;
import static io.metersphere.system.controller.handler.result.CommonResultCode.DEFAULT_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateService extends BaseTemplateService {

    @Resource
    private ProjectService projectService;
    @Resource
    private ServiceIntegrationService serviceIntegrationService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Override
    public List list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = super.list(projectId, scene);
        // 缺陷模板需要获取第三方平台模板
        templates = addPluginBugTemplate(projectId, scene, templates);
        // 标记默认模板
        return tagDefaultTemplate(projectId, scene, templates);
    }

    /**
     * 如果是缺陷模板，并且配置了服务集成和项目信息，则添加第三方平台模板
     * @param projectId
     * @param scene
     * @param templates
     * @return
     */
    private List<Template> addPluginBugTemplate(String projectId, String scene, List<Template> templates) {
        if (StringUtils.equals(scene, TemplateScene.BUG.name())) {
            Template pluginBugTemplate = getPluginBugTemplate(projectId);
            if (pluginBugTemplate != null) {
                templates.add(pluginBugTemplate);
            }
        }
        return templates;
    }

    /**
     * 获取当前项目中可用的模板选项
     * 默认模板排前面
     * !提供给其他模块调用
     * @param projectId
     * @param scene
     * @return
     */
    public List<ProjectTemplateOptionDTO> getOption(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = getTemplates(projectId, scene);
        translateInternalTemplate(templates);

        // 缺陷模板需要获取第三方平台模板
        templates = addPluginBugTemplate(projectId, scene, templates);

        List<ProjectTemplateDTO> projectTemplateDTOS = tagDefaultTemplate(projectId, scene, templates);
        return sortByDefaultTemplate(projectTemplateDTOS).stream()
                .map(item -> BeanUtils.copyBean(new ProjectTemplateOptionDTO(), item))
                .collect(Collectors.toList());
    }


    /**
     * 将默认模板排最前面
     * @param projectTemplateDTOS
     * @return
     */
    private static List<ProjectTemplateDTO> sortByDefaultTemplate(List<ProjectTemplateDTO> projectTemplateDTOS) {
        return projectTemplateDTOS.stream()
                .sorted(Comparator.comparing(ProjectTemplateDTO::getEnableDefault)) // 默认模板排在前面
                .toList();
    }

    /**
     * 获取默认的模板以及字段
     * !提供给其他模块调用
     * @param projectId
     * @param scene
     * @return
     */
    public TemplateDTO getDefaultTemplateDTO(String projectId, String scene) {
        String defaultTemplateId = getDefaultTemplateId(projectId, scene);
        Template template;
        if (StringUtils.isBlank(defaultTemplateId)) {
            // 如果没有默认模板，则获取内置模板
            template = getInternalTemplate(projectId, scene);
        } else {
            template = templateMapper.selectByPrimaryKey(defaultTemplateId);
            if (template == null) {
                // 如果默认模板查不到，则获取内置模板
                template = getInternalTemplate(projectId, scene);
            }
        }
        return getTemplateDTO(template);
    }

    /**
     * 获取内置模板
     * @param projectId
     * @param scene
     * @return
     */
    public Template getInternalTemplate(String projectId, String scene) {
        // 获取内置模板
        TemplateExample example = new TemplateExample();
        example.createCriteria()
                .andScopeIdEqualTo(projectId)
                .andSceneEqualTo(scene)
                .andInternalEqualTo(true);
        return templateMapper.selectByExample(example).get(0);
    }

    /**
     * 获取设置的默认模板ID
     *
     * @param projectId
     * @param scene
     * @return
     */
    public String getDefaultTemplateId(String projectId, String scene) {
        ProjectApplicationType.DEFAULT_TEMPLATE defaultTemplateParam = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(scene);
        ProjectApplication projectApplication = projectApplicationService.getByType(projectId, defaultTemplateParam.name());
        return projectApplication == null ? null : projectApplication.getTypeValue();
    }

    /**
     * 标记默认模板
     *
     * @param projectId
     * @param scene
     * @param templates
     * @return
     */
    private List<ProjectTemplateDTO> tagDefaultTemplate(String projectId, String scene, List<Template> templates) {
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(projectId, scene);
        List<ProjectTemplateDTO> templateDTOS = templates.stream().map(item -> BeanUtils.copyBean(new ProjectTemplateDTO(), item)).toList();

        ProjectTemplateDTO defaultTemplate = templateDTOS.stream()
                .filter(t -> StringUtils.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            defaultTemplate = templateDTOS.stream()
                    .filter(ProjectTemplateDTO::getInternal)
                    .findFirst()
                    .get();
        }
        defaultTemplate.setEnableDefault(true);
        return templateDTOS;
    }

    /**
     * 获取第三方平台模板
     *
     * @param projectId
     * @return
     */
    public Template getPluginBugTemplate(String projectId) {
        ServiceIntegration serviceIntegration = getServiceIntegration(projectId);
        if (serviceIntegration == null) {
            return null;
        }
        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(),
                serviceIntegration.getOrganizationId(), new String(serviceIntegration.getConfiguration()));
        if (platform != null && platform.isThirdPartTemplateSupport()) {
            return getPluginBugTemplate(projectId, serviceIntegration.getPluginId()); // 该插件支持第三方平台模板
        }
        return null;
    }

    private Template getPluginBugTemplate(String projectId, String pluginId) {
        PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(pluginId);
        Template template = new Template();
        template.setId(pluginWrapper.getPluginId());
        template.setName(((MsPlugin) pluginWrapper.getPlugin()).getName() + Translator.get("default_template"));
        template.setCreateUser(InternalUser.ADMIN.getValue());
        template.setScene(TemplateScene.BUG.name());
        template.setEnableThirdPart(true);
        template.setScopeId(projectId);
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setInternal(false);
        template.setRemark(((MsPlugin) pluginWrapper.getPlugin()).getName() + Translator.get("plugin_bug_template_remark"));
        return template;
    }

    /**
     * 如果项目下配置了第三方平台信息
     * 获取对应的服务集成信息
     *
     * @param projectId
     * @return
     */
    public ServiceIntegration getServiceIntegration(String projectId) {
        // 判断项目是否开启集成缺陷
        ProjectApplication syncEnableConfig = projectApplicationService.getByType(projectId, ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name());
        boolean isSyncEnable = syncEnableConfig != null && Boolean.parseBoolean(syncEnableConfig.getTypeValue());
        if (!isSyncEnable) {
            return null;
        }
        ProjectDTO project = projectService.getProjectById(projectId);
        // 查询组织下有权限的插件
        Set<String> orgPluginIds = platformPluginService.getOrgEnabledPlatformPlugins(project.getOrganizationId())
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());
        // 查询服务集成中启用并且支持第三方模板的插件
        return serviceIntegrationService.getServiceIntegrationByOrgId(project.getOrganizationId())
                .stream()
                .filter(serviceIntegration -> {
                    return serviceIntegration.getEnable()    // 服务集成开启
                            && orgPluginIds.contains(serviceIntegration.getPluginId());  // 该服务集成对应的插件有权限
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取模板以及字段
     * !提供给其他模块调用
     * @param id
     * @return
     */
    public TemplateDTO getTemplateDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkProjectResourceExist(template);
        return super.getTemplateDTO(template);
    }

    @Override
    public Template add(Template template, List<TemplateCustomFieldRequest> customFields) {
        checkProjectResourceExist(template);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setRefId(null);
        return super.add(template, customFields);
    }

    public void checkProjectResourceExist(Template template) {
        projectService.checkResourceExist(template.getScopeId());
    }

    @Override
    public Template update(Template template, List<TemplateCustomFieldRequest> customFields) {
        Template originTemplate = super.getWithCheck(template.getId());
        checkProjectTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        checkProjectResourceExist(originTemplate);
        return super.update(template, customFields);
    }

    @Override
    public void delete(String id) {
        Template template = getWithCheck(id);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        checkDefault(template);
        super.delete(id);
    }

    /**
     * 设置成默认模板后不能删除
     *
     * @param template
     */
    protected void checkDefault(Template template) {
        String defaultTemplateId = getDefaultTemplateId(template.getScopeId(), template.getScene());
        if (StringUtils.equals(template.getId(), defaultTemplateId)) {
            throw new MSException(DEFAULT_TEMPLATE_PERMISSION);
        }
    }

    /**
     * 将模板设置成默认模板
     *
     * @param id
     */
    public void setDefaultTemplate(String projectId, String id) {
        Template template = get(id);
        if (template == null) {
            Template pluginBugTemplate = getPluginBugTemplate(projectId);
            if (pluginBugTemplate != null && StringUtils.equals(pluginBugTemplate.getId(), id)) {
                template = pluginBugTemplate;
            }
        }
        if (template == null) {
            // 为空check抛出异常
            template = getWithCheck(id);
        }
        String paramType = ProjectApplicationType.DEFAULT_TEMPLATE.getByTemplateScene(template.getScene()).name();
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(projectId);
        projectApplication.setTypeValue(id);
        projectApplication.setType(paramType);
        projectApplicationService.createOrUpdateConfig(projectApplication);
    }


    public void checkProjectTemplateEnable(String projectId, String scene) {
        ProjectDTO project = projectService.getProjectById(projectId);
        if (isOrganizationTemplateEnable(project.getOrganizationId(), scene)) {
            throw new MSException(PROJECT_TEMPLATE_PERMISSION);
        }
    }
}