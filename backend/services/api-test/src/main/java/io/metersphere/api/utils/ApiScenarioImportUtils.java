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

    public static Map<String, ApiDefinitionDetail> getApiIdInTargetList(List<ApiDefinitionDetail> compareList, List<ApiDefinitionDetail> thisProjectCompareList, List<ApiDefinitionDetail> targetList, String protocol, String projectId, ApiScenarioPreImportAnalysisResult analysisResult) {
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
        Map<String, ApiDefinitionDetail> apiIdDic = new HashMap<>();

        List<ApiDefinitionDetail> notCompareList = new ArrayList<>();
        for (ApiDefinitionDetail compareApi : compareList) {
            String compareKey = StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL) ?
                    compareApi.getMethod() + compareApi.getPath() : compareApi.getModulePath() + compareApi.getName();
            // 去除文件中相同类型的接口
            if (targetApiIdMap.containsKey(compareKey)) {
                apiIdDic.put(compareApi.getId(), targetApiIdMap.get(compareKey));
            } else {
                if (prepareInsertApi.containsKey(compareKey)) {
                    apiIdDic.put(compareApi.getId(), prepareInsertApi.get(compareKey));
                } else {
                    // 目标项目找不到，尝试找一下当前项目
                    notCompareList.add(compareApi);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(notCompareList)) {
            // 使用当前项目进行匹配
            targetApiIdMap = null;

            if (StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL)) {
                targetApiIdMap = thisProjectCompareList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
            } else {
                targetApiIdMap = thisProjectCompareList.stream().collect(Collectors.toMap(t -> t.getModulePath() + t.getName(), t -> t, (oldValue, newValue) -> newValue));
            }

            for (ApiDefinitionDetail compareApi : compareList) {
                String compareKey = StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL) ?
                        compareApi.getMethod() + compareApi.getPath() : compareApi.getModulePath() + compareApi.getName();
                // 去除文件中相同类型的接口
                if (targetApiIdMap.containsKey(compareKey)) {
                    apiIdDic.put(compareApi.getId(), targetApiIdMap.get(compareKey));
                } else {
                    if (prepareInsertApi.containsKey(compareKey)) {
                        apiIdDic.put(compareApi.getId(), prepareInsertApi.get(compareKey));
                    } else {
                        // 目标项目找不到，尝试找一下当前项目
                        String oldId = compareApi.getId();
                        compareApi.setProjectId(projectId);
                        compareApi.setId(IDGenerator.nextStr());
                        analysisResult.setApiDefinition(compareApi);
                        apiIdDic.put(oldId, compareApi);
                        prepareInsertApi.put(compareKey, compareApi);

                        // 添加当前项目中，用于后面的匹配
                        thisProjectCompareList.add(compareApi);
                    }
                }
            }
        }
        return apiIdDic;
    }

    public static ApiDefinitionDetail isApiExistence(String protocol, String method, String path, String modulePath, String apiDefinitionName, List<ApiDefinitionDetail> existenceApiDefinitionList) {
        Map<String, ApiDefinitionDetail> existenceMap = null;
        if (StringUtils.equalsIgnoreCase(protocol, ApiConstants.HTTP_PROTOCOL)) {
            existenceMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t, (oldValue, newValue) -> newValue));
            if (existenceMap.containsKey(method + path)) {
                return existenceMap.get(method + path);
            }
        } else {
            existenceMap = existenceApiDefinitionList.stream().collect(Collectors.toMap(t -> t.getModulePath() + t.getName(), t -> t, (oldValue, newValue) -> newValue));
            if (existenceMap.containsKey(modulePath + apiDefinitionName)) {
                return existenceMap.get(modulePath + apiDefinitionName);
            }
        }
        return null;
    }
}
