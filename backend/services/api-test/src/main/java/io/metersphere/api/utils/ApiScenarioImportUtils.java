package io.metersphere.api.utils;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.converter.ApiDefinitionDetail;
import io.metersphere.api.dto.converter.ApiScenarioPreImportAnalysisResult;
import io.metersphere.project.domain.Project;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.uid.IDGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ApiScenarioImportUtils {
    public static LogDTO genImportLog(Project project, String dataId, String dataName, Object targetList, String module, String operator, String operationType) {
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
        dto.setOriginalValue(JSON.toJSONBytes(targetList));
        return dto;
    }

    public static Map<String, String> getApiIdInTargetList(List<ApiDefinitionDetail> compareList, List<ApiDefinitionDetail> targetList, String protocol, String projectId, ApiScenarioPreImportAnalysisResult analysisResult) {
        if (CollectionUtils.isEmpty(compareList)) {
            return new HashMap<>();
        }
        if (targetList == null) {
            targetList = new ArrayList<>();
        }

        //        API类型，通过 Method & Path 组合判断，接口是否存在
        Map<String, ApiDefinitionDetail> targetApiIdMap = null;

        if (StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL)) {
            targetApiIdMap = targetList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
        } else {
            targetApiIdMap = targetList.stream().collect(Collectors.toMap(t -> t.getModulePath() + t.getName(), t -> t, (oldValue, newValue) -> newValue));
        }
        Map<String, ApiDefinitionDetail> prepareInsertApi = new HashMap<>();
        Map<String, String> apiIdDic = new HashMap<>();
        for (ApiDefinitionDetail compareApi : compareList) {
            String compareKey = StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL) ?
                    compareApi.getMethod() + compareApi.getPath() : compareApi.getModulePath() + compareApi.getName();
            // 去除文件中相同类型的接口

            if (targetApiIdMap.containsKey(compareKey)) {
                apiIdDic.put(compareApi.getId(), targetApiIdMap.get(compareKey).getId());
            } else {
                if (prepareInsertApi.containsKey(compareKey)) {
                    apiIdDic.put(compareApi.getId(), prepareInsertApi.get(compareKey).getId());
                } else {
                    String oldId = compareApi.getId();
                    compareApi.setProjectId(projectId);
                    compareApi.setId(IDGenerator.nextStr());
                    analysisResult.setApiDefinition(compareApi);
                    apiIdDic.put(oldId, compareApi.getId());
                    prepareInsertApi.put(compareKey, compareApi);
                }
            }
        }
        return apiIdDic;
    }

    public static boolean isApiExistence(String protocol, String method, String path, String modulePath, String apiDefinitionName, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        Map<String, ApiDefinitionDetail> existenceMap = null;
        if (StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL)) {
            existenceMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
            return existenceMap.containsKey(method + path);
        } else {
            existenceMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getModulePath() + t.getName(), t -> t, (oldValue, newValue) -> newValue));
            return existenceMap.containsKey(modulePath + apiDefinitionName);
        }
    }
}
