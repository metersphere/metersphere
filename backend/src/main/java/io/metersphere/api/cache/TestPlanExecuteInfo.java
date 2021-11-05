package io.metersphere.api.cache;


import io.metersphere.api.jmeter.JmeterThreadUtils;
import io.metersphere.api.jmeter.MessageCache;
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
    private String creator;
    private Map<String, String> apiCaseExecInfo = new HashMap<>();
    private Map<String, String> apiScenarioCaseExecInfo = new HashMap<>();
    private Map<String, String> loadCaseExecInfo = new HashMap<>();

    private Map<String, String> apiCaseExecuteReportMap = new HashMap<>();
    private Map<String, String> apiScenarioReportReportMap = new HashMap<>();
    private Map<String, String> loadCaseReportIdMap = new HashMap<>();

    private boolean reportDataInDataBase;

    int lastUnFinishedNumCount = 0;
    long lastFinishedNumCountTime = 0;

    private boolean isApiCaseAllExecuted;
    private boolean isScenarioAllExecuted;
    private boolean isLoadCaseAllExecuted;

    public TestPlanExecuteInfo(String reportId, String creator) {
        this.reportId = reportId;
        this.creator = creator;
    }

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

    public synchronized void updateExecuteResult(Map<String, String> apiCaseExecResultInfo, Map<String, String> apiScenarioCaseExecResultInfo, Map<String, String> loadCaseExecResultInfo) {
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
                if (StringUtils.isNotEmpty(apiCaseExecuteReportMap.get(resourceId))) {
                    JmeterThreadUtils.stop(apiCaseExecuteReportMap.get(resourceId));
                }
            }

            if (apiCaseExecuteReportMap.containsKey(resourceId)) {
                MessageCache.executionQueue.remove(apiCaseExecuteReportMap.get(resourceId));
            }
        }
        for (Map.Entry<String, String> entry : apiScenarioCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                apiScenarioCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
                if (StringUtils.isNotEmpty(apiScenarioReportReportMap.get(resourceId))) {
                    JmeterThreadUtils.stop(apiScenarioReportReportMap.get(resourceId));
                }
            }

            if (apiScenarioReportReportMap.containsKey(resourceId)) {
                MessageCache.executionQueue.remove(apiScenarioReportReportMap.get(resourceId));
            }
        }
        for (Map.Entry<String, String> entry : loadCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                if (StringUtils.isNotEmpty(loadCaseReportIdMap.get(resourceId))) {
                    JmeterThreadUtils.stop(loadCaseReportIdMap.get(resourceId));
                }
                loadCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
            }

            if (loadCaseReportIdMap.containsKey(resourceId)) {
                MessageCache.executionQueue.remove(loadCaseReportIdMap.get(resourceId));
            }
        }

        this.countUnFinishedNum();
    }
}