package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.system.dto.sdk.CustomFieldDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldOptionRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.service.BaseCustomFieldService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.project.enums.result.ProjectResultCode.PROJECT_TEMPLATE_PERMISSION;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectCustomFieldService extends BaseCustomFieldService {

    @Resource
    private ProjectService projectService;

    @Override
    public List<CustomFieldDTO> list(String projectId, String scene) {
        projectService.checkResourceExist(projectId);
        return super.list(projectId, scene);
    }

    @Override
    public CustomFieldDTO getCustomFieldDTOWithCheck(String id) {
        CustomFieldDTO customField = super.getCustomFieldDTOWithCheck(id);
        projectService.checkResourceExist(customField.getScopeId());
        return customField;
    }

    @Override
    public CustomField add(CustomField customField, List<CustomFieldOptionRequest> options) {
        Project project = projectService.checkResourceExist(customField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), customField.getScene());
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        return super.add(customField, options);
    }

    @Override
    public CustomField update(CustomField customField, List<CustomFieldOptionRequest> options) {
        CustomField originCustomField = getWithCheck(customField.getId());
        if (originCustomField.getInternal()) {
            // 内置字段不能修改名字
            customField.setName(null);
        }
        customField.setScopeId(originCustomField.getScopeId());
        customField.setScene(originCustomField.getScene());
        Project project = projectService.checkResourceExist(originCustomField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), originCustomField.getScene());
        return super.update(customField, options);
    }

    @Override
    public void delete(String id) {
        CustomField customField = getWithCheck(id);
        checkInternal(customField);
        Project project = projectService.checkResourceExist(customField.getScopeId());
        checkProjectTemplateEnable(project.getOrganizationId(), customField.getScene());
        super.delete(id);
    }

    private void checkProjectTemplateEnable(String orgId, String scene) {
        if (isOrganizationTemplateEnable(orgId, scene)) {
            throw new MSException(PROJECT_TEMPLATE_PERMISSION);
        }
    }
}