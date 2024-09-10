package io.metersphere.api.utils;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.dto.LogDTO;


public class ApiScenarioImportUtils {
    public static LogDTO genImportLog(Project project, String dataId, String dataName, Object importData, String module, String operator, String operationType) {
        LogDTO dto = new LogDTO(
                project.getId(),
                project.getOrganizationId(),
                dataId,
                operator,
                operationType,
                module,
                dataName);
        dto.setHistory(true);
        dto.setPath("/api/scenario/import");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(importData));
        return dto;
    }
}
