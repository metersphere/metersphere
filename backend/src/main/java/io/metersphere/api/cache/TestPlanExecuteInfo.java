package io.metersphere.api.cache;


import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import io.metersphere.commons.constants.TestPlanResourceType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/21 5:15 下午
 */
@Getter
@Setter
public class TestPlanExecuteInfo {
    private String reportId;
    private Map<String, String> apiCaseExecInfo = new HashMap<>();
    private Map<String, String> apiScenarioCaseExecInfo = new HashMap<>();
    private Map<String, String> loadCaseExecInfo = new HashMap<>();

    private Map<String, ApiDefinitionExecResult> apiCaseExecuteReportMap = new HashMap<>();
    private Map<String, APIScenarioReportResult> apiScenarioReportReportMap = new HashMap<>();
    private Map<String,String> loadCaseReportIdMap = new HashMap<>();

    private boolean reportDataInDataBase;

    int lastUnFinishedNumCount = 0;
    long lastFinishedNumCountTime = 0;

    private boolean isApiCaseAllExecuted;
    private boolean isScenarioAllExecuted;
    private boolean isLoadCaseAllExecuted;

    public synchronized void updateExecuteInfo(Map<String, String> apiCaseExecInfo, Map<String, String> apiScenarioCaseExecInfo, Map<String, String> loadCaseExecInfo) {
        if (MapUtils.isNotEmpty(apiCaseExecInfo)) {
            this.apiCaseExecInfo.putAll(apiCaseExecInfo);
        }

        if (MapUtils.isNotEmpty(apiScenarioCaseExecInfo)) {
            this.apiScenarioCaseExecInfo.putAll(apiScenarioCaseExecInfo);
        }

        if (MapUtils.isNotEmpty(loadCaseExecInfo)) {
            this.loadCaseExecInfo.putAll(loadCaseExecInfo);
        }
    }

    public synchronized void updateExecuteResult(Map<String, ApiDefinitionExecResult> apiCaseExecResultInfo, Map<String, APIScenarioReportResult> apiScenarioCaseExecResultInfo, Map<String, String> loadCaseExecResultInfo) {
        if (MapUtils.isNotEmpty(apiCaseExecResultInfo)) {
            this.apiCaseExecuteReportMap.putAll(apiCaseExecResultInfo);
        }

        if (MapUtils.isNotEmpty(apiScenarioCaseExecResultInfo)) {
            this.apiScenarioReportReportMap.putAll(apiScenarioCaseExecResultInfo);
        }

        if (MapUtils.isNotEmpty(loadCaseExecResultInfo)) {
            this.loadCaseReportIdMap.putAll(loadCaseExecResultInfo);
        }
    }

    public synchronized int countUnFinishedNum() {
        int unFinishedCount = 0;

        this.isApiCaseAllExecuted = true;
        this.isScenarioAllExecuted = true;
        this.isLoadCaseAllExecuted = true;

        for (String result : apiCaseExecInfo.values()) {
            if (StringUtils.equalsIgnoreCase(result, TestPlanApiExecuteStatus.RUNNING.name())) {
                unFinishedCount++;
                if (this.isApiCaseAllExecuted) {
                    this.isApiCaseAllExecuted = false;
                }
            }
        }
        for (String result : apiScenarioCaseExecInfo.values()) {
            if (StringUtils.equalsIgnoreCase(result, TestPlanApiExecuteStatus.RUNNING.name())) {
                unFinishedCount++;
                if (this.isScenarioAllExecuted) {
                    isScenarioAllExecuted = false;
                }
            }
        }
        for (String result : loadCaseExecInfo.values()) {
            if (StringUtils.equalsIgnoreCase(result, TestPlanApiExecuteStatus.RUNNING.name())) {
                unFinishedCount++;
                if (this.isLoadCaseAllExecuted) {
                    isLoadCaseAllExecuted = false;
                }
            }
        }
        if (lastUnFinishedNumCount != unFinishedCount) {
            lastUnFinishedNumCount = unFinishedCount;
            lastFinishedNumCountTime = System.currentTimeMillis();
        }
        return unFinishedCount;
    }

    public Map<String, Map<String, String>> getExecutedResult() {
        Map<String, Map<String, String>> resourceTypeMap = new HashMap<>();

        for (Map.Entry<String, String> entry : apiCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            String resourceType = TestPlanResourceType.API_CASE.name();

            if (resourceTypeMap.containsKey(resourceType)) {
                resourceTypeMap.get(resourceType).put(resourceId, executeResult);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put(resourceId, executeResult);
                resourceTypeMap.put(resourceType, map);
            }
        }

        for (Map.Entry<String, String> entry : apiScenarioCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            String resourceType = TestPlanResourceType.SCENARIO_CASE.name();

            if (resourceTypeMap.containsKey(resourceType)) {
                resourceTypeMap.get(resourceType).put(resourceId, executeResult);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put(resourceId, executeResult);
                resourceTypeMap.put(resourceType, map);
            }
        }

        for (Map.Entry<String, String> entry : loadCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            String resourceType = TestPlanResourceType.PERFORMANCE_CASE.name();

            if (resourceTypeMap.containsKey(resourceType)) {
                resourceTypeMap.get(resourceType).put(resourceId, executeResult);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put(resourceId, executeResult);
                resourceTypeMap.put(resourceType, map);
            }
        }

        return resourceTypeMap;
    }

    public void finishAllTask() {
        for (Map.Entry<String, String> entry : apiCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                apiCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
            }
        }
        for (Map.Entry<String, String> entry : apiScenarioCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                apiScenarioCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
            }
        }
        for (Map.Entry<String, String> entry : loadCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                loadCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
            }
        }

        this.countUnFinishedNum();
    }
}