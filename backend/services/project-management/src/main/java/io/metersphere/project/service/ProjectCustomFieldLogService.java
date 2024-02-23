package io.metersphere.project.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.dto.sdk.request.CustomFieldUpdateRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectCustomFieldLogService {

    @Resource
    private ProjectCustomFieldService projectCustomFieldService;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                null,
                null,
                null,
                null,
                OperationLogType.ADD.name(),
                getOperationLogModule(request.getScene()),
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public String getOperationLogModule(String scene) {
        TemplateScene templateScene = EnumValidator.validateEnum(TemplateScene.class, scene);
        switch (templateScene) {
            case API:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_API_FIELD;
            case FUNCTIONAL:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_FIELD;
            case UI:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_UI_FIELD;
            case BUG:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_BUG_FIELD;
            case TEST_PLAN:
                return OperationLogModule.PROJECT_MANAGEMENT_TEMPLATE_TEST_PLAN_FIELD;
            default:
                return null;
        }
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = projectCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                null,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }

    public LogDTO deleteLog(String id) {
        CustomField customField = projectCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                null,
                null,
                customField.getId(),
                null,
                OperationLogType.DELETE.name(),
                getOperationLogModule(customField.getScene()),
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }
}