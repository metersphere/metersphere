package io.metersphere.api.utils;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.dto.LogDTO;


public class ApiDefinitionImportUtils {
    private static final String FILE_JMX = "jmx";
    private static final String FILE_HAR = "har";
    private static final String FILE_JSON = "json";

    public static void checkFileSuffixName(ImportRequest request, String suffixName) {
        if (FILE_JMX.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_HAR.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_JSON.equalsIgnoreCase(suffixName)) {
            if (ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform()) || ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
    }

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
        dto.setPath("/api/definition/import");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(importData));
        return dto;
    }
}
