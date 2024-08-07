package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.dto.sdk.request.CustomFieldUpdateRequest;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jianxing
 * @date : 2023-8-29
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationCustomFieldLogService {

    @Resource
    private OrganizationCustomFieldService organizationCustomFieldService;

    public LogDTO addLog(CustomFieldUpdateRequest request) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
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
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_API_FIELD;
            case FUNCTIONAL:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_FIELD;
            case UI:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_UI_FIELD;
            case BUG:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_BUG_FIELD;
            case TEST_PLAN:
                return OperationLogModule.SETTING_ORGANIZATION_TEMPLATE_TEST_PLAN_FIELD;
            default:
                return null;
        }
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = organizationCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
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
        CustomField customField = organizationCustomFieldService.getWithCheck(id);
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
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