package io.metersphere.project.service;

import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.CustomFieldOptions;
import io.metersphere.project.dto.ProjectTemplateDTO;
import io.metersphere.project.dto.ProjectTemplateOptionDTO;
import io.metersphere.sdk.constants.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.ProjectDTO;
import io.metersphere.system.dto.sdk.TemplateDTO;
import io.metersphere.system.dto.sdk.request.TemplateUpdateRequest;
import io.metersphere.system.dto.user.UserExtendDTO;
import io.metersphere.system.mapper.CustomFieldOptionMapper;
import io.metersphere.system.service.BaseTemplateService;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.PluginLoadService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    private PluginLoadService pluginLoadService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    @Resource
    private CustomFieldOptionMapper customFieldOptionMapper;

    @Override
    public List list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        List<Template> templates = super.list(projectId, scene);
        List<ProjectTemplateDTO> templateDTOS = templates.stream().map(item -> BeanUtils.copyBean(new ProjectTemplateDTO(), item)).collect(Collectors.toList());
        // 缺陷模板需要获取第三方平台模板
        if (StringUtils.equals(scene, TemplateScene.BUG.name())) {
            Template pluginBugTemplate = getPluginBugTemplate(projectId);
            if (pluginBugTemplate != null) {
                ProjectTemplateDTO pluginTemplate = BeanUtils.copyBean(new ProjectTemplateDTO(), pluginBugTemplate);
                pluginTemplate.setEnablePlatformDefault(true);
                templateDTOS.add(pluginTemplate);
            }
        }

        // 标记默认模板
        // 查询项目下设置中配置的默认模板
        String defaultProjectId = getDefaultTemplateId(projectId, scene);
        ProjectTemplateDTO defaultTemplate = templateDTOS.stream()
                .filter(t -> StringUtils.equals(defaultProjectId, t.getId()))
                .findFirst()
                .orElse(null);

        // 如果查询不到默认模板，设置内置模板为默认模板
        if (defaultTemplate == null) {
            Optional<ProjectTemplateDTO> internalTemplate = templateDTOS.stream()
                    .filter(ProjectTemplateDTO::getInternal).findFirst();
            if (internalTemplate.isPresent()) {
                defaultTemplate = internalTemplate.get();
            }
        }
        if (defaultTemplate != null) {
            defaultTemplate.setEnableDefault(true);
        }
        return templateDTOS;
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
        TemplateDTO templateDTO = getTemplateDTO(template);
        List<CustomFieldOption> memberCustomOption = getMemberOptions(projectId);
        templateDTO.getCustomFields().forEach(item -> {
            if (StringUtils.equalsAnyIgnoreCase(item.getType(), CustomFieldType.MEMBER.name(), CustomFieldType.MULTIPLE_MEMBER.name())) {
                item.setOptions(memberCustomOption);
            }
        });
        return templateDTO;
    }

    public List<CustomFieldOption> getMemberOptions(String projectId) {
        List<UserExtendDTO> memberOption = projectService.getMemberOption(projectId, null);
        return memberOption.stream().map(option -> {
            CustomFieldOption customFieldOption = new CustomFieldOption();
            customFieldOption.setFieldId(option.getId());
            customFieldOption.setValue(option.getId());
            customFieldOption.setInternal(false);
            customFieldOption.setText(option.getName());
            return customFieldOption;
        }).toList();
    }


    /**
     * 获取模板自定义字段
     * @param templateId
     * @param projectId
     * @param scene
     * @return
     */
    public TemplateDTO getTemplateDTOById(String templateId, String projectId, String scene) {
        AtomicReference<TemplateDTO> templateDTO = new AtomicReference<>(new TemplateDTO());
        Template template = templateMapper.selectByPrimaryKey(templateId);
        Optional.ofNullable(template).ifPresentOrElse(item -> {
            templateDTO.set(super.getTemplateDTO(template));
        }, () -> {
            templateDTO.set(getDefaultTemplateDTO(projectId, scene));
        });
        return templateDTO.get();
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
        return templateMapper.selectByExample(example).getFirst();
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
        ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
        if (serviceIntegration == null) {
            return null;
        }
        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(),
                serviceIntegration.getOrganizationId(), new String(serviceIntegration.getConfiguration()));
        if (platform != null && platform.isSupportDefaultTemplate()) {
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
     * 获取模板以及字段
     * @param id
     * @return
     */
    public TemplateDTO getTemplateDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkProjectResourceExist(template);
        TemplateDTO templateDTO = super.getTemplateDTO(template);
        translateInternalTemplate(List.of(templateDTO));
        return templateDTO;
    }

    public Template add(TemplateUpdateRequest request, String creator) {
        Template template = BeanUtils.copyBean(new Template(), request);
        template.setCreateUser(creator);
        checkProjectResourceExist(template);
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        template.setScopeType(TemplateScopeType.PROJECT.name());
        template.setRefId(null);
        template = super.add(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
    }

    public void checkProjectResourceExist(Template template) {
        projectService.checkResourceExist(template.getScopeId());
    }

    public Template update(TemplateUpdateRequest request) {
        Template template = new Template();
        BeanUtils.copyBean(template, request);
        Template originTemplate = super.getWithCheck(template.getId());
        if (originTemplate.getInternal()) {
            // 内置模板不能修改名字
            template.setName(null);
        }
        checkProjectTemplateEnable(originTemplate.getScopeId(), originTemplate.getScene());
        template.setScopeId(originTemplate.getScopeId());
        template.setScene(originTemplate.getScene());
        checkProjectResourceExist(originTemplate);
        template = super.update(template, request.getCustomFields(), request.getSystemFields());
        saveUploadImages(request);
        return template;
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

    /**
     * 一个接口返回各个模板是否启用项目模板
     * @param projectId
     * @return
     */
    public Map<String, Boolean> getProjectTemplateEnableConfig(String projectId) {
        ProjectService.checkResourceExist(projectId);
        ProjectDTO project = projectService.getProjectById(projectId);
        HashMap<String, Boolean> templateEnableConfig = new HashMap<>();
        List.of(TemplateScene.FUNCTIONAL, TemplateScene.BUG)
                .forEach(scene ->
                        templateEnableConfig.put(scene.name(), !isOrganizationTemplateEnable(project.getOrganizationId(), scene.name())));
        return templateEnableConfig;
    }

    /**
     * 获取表头可设置自定义字段（接口提供）
     * @param projectId
     * @param scene
     * @return
     */
    public List<CustomFieldOptions> getTableCustomField(String projectId, String scene) {
        TemplateExample example = new TemplateExample();
        example.createCriteria().andScopeIdEqualTo(projectId).andSceneEqualTo(scene);
        List<Template> templates = templateMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(templates)) {
            List<String> templateIds = templates.stream().map(Template::getId).collect(Collectors.toList());
            List<TemplateCustomField> fieldList = baseTemplateCustomFieldService.getByTemplateIds(templateIds);
            List<String> fieldIds = fieldList.stream().map(TemplateCustomField::getFieldId).distinct().collect(Collectors.toList());
            List<CustomField> customFields = baseCustomFieldService.getByIds(fieldIds);
            CustomFieldOptionExample optionExample = new CustomFieldOptionExample();
            optionExample.createCriteria().andFieldIdIn(fieldIds);
            List<CustomFieldOption> customFieldOptions = customFieldOptionMapper.selectByExample(optionExample);
            Map<String, List<CustomFieldOption>> optionMap = customFieldOptions.stream().collect(Collectors.groupingBy(CustomFieldOption::getFieldId));
            List<CustomFieldOptions> collect = customFields.stream().map(customField -> {
                CustomFieldOptions optionDTO = new CustomFieldOptions();
                optionDTO.setId(customField.getId());
                if (customField.getInternal()) {
                    optionDTO.setInternalFieldKey(customField.getName());
                    optionDTO.setName(baseCustomFieldService.translateInternalField(customField.getName()));
                } else {
                    optionDTO.setName(customField.getName());
                }
                optionDTO.setOptions(optionMap.get(customField.getId()));
                optionDTO.setInternal(customField.getInternal());
                optionDTO.setType(customField.getType());
                return optionDTO;
            }).collect(Collectors.toList());
            return collect;
        }
        return new ArrayList<>();
    }

    /**
     * 保存上传的文件
     * 将文件从临时目录移动到正式目录
     *
     * @param request
     */
    private void saveUploadImages(TemplateUpdateRequest request) {
        String projectTemplateDir = DefaultRepositoryDir.getProjectTemplateImgDir(request.getScopeId());
        String projectTemplatePreviewDir = DefaultRepositoryDir.getProjectTemplateImgPreviewDir(request.getScopeId());
        commonFileService.saveReviewImgFromTempFile(projectTemplateDir, projectTemplatePreviewDir, request.getUploadImgFileIds());
    }

    /**
     * 富文本框图片预览
     * @param projectId
     * @param fileId
     * @param compressed
     * @return
     */
    public ResponseEntity<byte[]> previewImg(String projectId, String fileId, boolean compressed) {
        String projectTemplateDir = DefaultRepositoryDir.getProjectTemplateImgDir(projectId);
        String projectTemplatePreviewDir = DefaultRepositoryDir.getProjectTemplateImgPreviewDir(projectId);
        return super.previewImg(fileId, projectTemplateDir, projectTemplatePreviewDir, compressed);
    }
}