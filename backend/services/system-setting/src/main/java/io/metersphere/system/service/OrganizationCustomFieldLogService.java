package io.metersphere.system.service;

import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.CustomFieldUpdateRequest;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.CustomField;
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
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION_CUSTOM_FIELD,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = organizationCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                OperationLogConstants.ORGANIZATION,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION_CUSTOM_FIELD,
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
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION_CUSTOM_FIELD,
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }
}