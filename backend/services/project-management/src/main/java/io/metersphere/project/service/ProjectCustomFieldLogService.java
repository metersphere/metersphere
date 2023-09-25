package io.metersphere.project.service;

import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.request.CustomFieldUpdateRequest;
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
                OperationLogModule.PROJECT_CUSTOM_FIELD,
                request.getName());
        dto.setOriginalValue(JSON.toJSONBytes(request));
        return dto;
    }

    public LogDTO updateLog(CustomFieldUpdateRequest request) {
        CustomField customField = projectCustomFieldService.getWithCheck(request.getId());
        LogDTO dto = new LogDTO(
                null,
                null,
                customField.getId(),
                null,
                OperationLogType.UPDATE.name(),
                OperationLogModule.PROJECT_CUSTOM_FIELD,
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
                OperationLogModule.PROJECT_CUSTOM_FIELD,
                customField.getName());
        dto.setOriginalValue(JSON.toJSONBytes(customField));
        return dto;
    }
}