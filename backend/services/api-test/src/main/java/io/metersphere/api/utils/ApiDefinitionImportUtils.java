package io.metersphere.api.utils;

import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.log.dto.LogDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


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

    public static List<ApiDefinitionDetail> apiRename(List<ApiDefinitionDetail> caseList) {
        List<ApiDefinitionDetail> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionDetail apiCase : caseList) {
                String uniqueName = getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

    public static List<ApiTestCaseDTO> apiCaseRename(List<ApiTestCaseDTO> caseList) {
        List<ApiTestCaseDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiTestCaseDTO apiCase : caseList) {
                String uniqueName = getUniqueName(apiCase.getName(), caseNameList);
                apiCase.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiCase);
            }
        }
        return returnList;
    }

    public static List<ApiDefinitionMockDTO> apiMockRename(List<ApiDefinitionMockDTO> caseList) {
        List<ApiDefinitionMockDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseList)) {
            List<String> caseNameList = new ArrayList<>();
            for (ApiDefinitionMockDTO apiMock : caseList) {
                String uniqueName = ApiDefinitionImportUtils.getUniqueName(apiMock.getName(), caseNameList);
                apiMock.setName(uniqueName);
                caseNameList.add(uniqueName);
                returnList.add(apiMock);
            }
        }
        return returnList;
    }

    private static String getUniqueName(String originalName, List<String> existenceNameList) {
        String returnName = originalName;
        int index = 1;
        while (existenceNameList.contains(returnName)) {
            returnName = originalName + " - " + index;
            index++;
        }
        return returnName;
    }
}
