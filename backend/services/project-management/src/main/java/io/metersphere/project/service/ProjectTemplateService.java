package io.metersphere.project.service;

import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.dto.ProjectExtendDTO;
import io.metersphere.sdk.dto.TemplateDTO;
import io.metersphere.sdk.dto.request.TemplateCustomFieldRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Template;
import io.metersphere.system.service.BaseTemplateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectTemplateService extends BaseTemplateService {

    @Resource
    private ProjectService projectService;

    @Override
    public List<Template> list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        return super.list(projectId, scene);
    }

    public TemplateDTO geDTOWithCheck(String id) {
        Template template = super.getWithCheck(id);
        checkProjectResourceExist(template);
        return super.geDTOWithCheck(template);
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
        checkProjectTemplateEnable(template.getScopeId(), template.getScene());
        super.setDefaultTemplate(id);
    }


    private void checkProjectTemplateEnable(String projectId, String scene) {
        ProjectExtendDTO project = projectService.getProjectById(projectId);
        if (isOrganizationTemplateEnable(project.getOrganizationId(), scene)) {
            throw new MSException(PROJECT_TEMPLATE_PERMISSION);
        }
    }
}