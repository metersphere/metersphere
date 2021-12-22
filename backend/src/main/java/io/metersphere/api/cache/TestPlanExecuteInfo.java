package io.metersphere.api.cache;


import io.metersphere.api.exec.queue.ExecThreadPoolExecutor;
import io.metersphere.api.jmeter.JmeterThreadUtils;
import io.metersphere.base.domain.ApiScenarioReport;
import io.metersphere.base.domain.ApiScenarioReportExample;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import io.metersphere.commons.constants.TestPlanResourceType;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.track.dto.TestPlanReportExecuteCheckResultDTO;
import io.metersphere.utils.LoggerUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author song.tianyang
 * @Date 2021/8/21 5:15 下午
 */
@Getter
@Setter
public class TestPlanExecuteInfo {
    private String reportId;
    private String creator;
    private Map<String, String> apiCaseExecInfo = new ConcurrentHashMap<>();
    private Map<String, String> apiScenarioCaseExecInfo = new ConcurrentHashMap<>();
    private Map<String, String> loadCaseExecInfo = new ConcurrentHashMap<>();

    private Map<String, String> apiCaseExecuteThreadMap = new ConcurrentHashMap<>();
    private Map<String, String> apiScenarioThreadMap = new ConcurrentHashMap<>();
    private Map<String, String> loadCaseReportIdMap = new ConcurrentHashMap<>();

    private Map<String, String> apiCaseReportMap = new ConcurrentHashMap<>();
    private Map<String, String> apiScenarioReportMap = new ConcurrentHashMap<>();
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

    public synchronized void updateThreadResult(Map<String, String> apiCaseExecResultInfo, Map<String, String> apiScenarioCaseExecResultInfo, Map<String, String> loadCaseExecResultInfo) {
        if (MapUtils.isNotEmpty(apiCaseExecResultInfo)) {
            this.apiCaseExecuteThreadMap.putAll(apiCaseExecResultInfo);
        }

        if (MapUtils.isNotEmpty(apiScenarioCaseExecResultInfo)) {
            this.apiScenarioThreadMap.putAll(apiScenarioCaseExecResultInfo);
        }

        if (MapUtils.isNotEmpty(loadCaseExecResultInfo)) {
            this.loadCaseReportIdMap.putAll(loadCaseExecResultInfo);
        }
    }

    public synchronized TestPlanReportExecuteCheckResultDTO countUnFinishedNum() {
        TestPlanReportExecuteCheckResultDTO executeCheck = new TestPlanReportExecuteCheckResultDTO();
        int unFinishedCount = 0;

        this.isApiCaseAllExecuted = true;
        this.isScenarioAllExecuted = true;
        this.isLoadCaseAllExecuted = true;
        boolean isQueue = false;
        for (String result : apiCaseExecInfo.values()) {
            if (StringUtils.equalsIgnoreCase(result, TestPlanApiExecuteStatus.RUNNING.name())) {
                unFinishedCount++;
                if (this.isApiCaseAllExecuted) {
                    this.isApiCaseAllExecuted = false;
                }
                if (!isQueue) {
                    isQueue = CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).check(apiCaseExecuteThreadMap.get(result));
                }
            }
        }
        for (String result : apiScenarioCaseExecInfo.values()) {
            if (StringUtils.equalsIgnoreCase(result, TestPlanApiExecuteStatus.RUNNING.name())) {
                unFinishedCount++;
                if (this.isScenarioAllExecuted) {
                    isScenarioAllExecuted = false;
                }
                if (!isQueue) {
                    isQueue = CommonBeanFactory.getBean(ExecThreadPoolExecutor.class).check(apiScenarioThreadMap.get(result));
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

        if (lastUnFinishedNumCount == unFinishedCount && isQueue) {
            LoggerUtil.info("执行的报告还在队列中，重置超时时间");
            lastUnFinishedNumCount = unFinishedCount;
            lastFinishedNumCountTime = System.currentTimeMillis();
            executeCheck.setFinishedCaseChanged(true);
        } else if (unFinishedCount == 0) {
            executeCheck.setFinishedCaseChanged(true);
        } else {
            executeCheck.setFinishedCaseChanged(false);
        }

        executeCheck.setTimeOut(false);
        if (unFinishedCount > 0) {
            //20分钟没有案例执行结果更新，则定位超时
            long nowTime = System.currentTimeMillis();
            if (nowTime - lastFinishedNumCountTime > 1200000) {
                executeCheck.setTimeOut(true);
            }
        }
        return executeCheck;
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
                if (StringUtils.isNotEmpty(apiCaseExecuteThreadMap.get(resourceId))) {
                    JmeterThreadUtils.stop(apiCaseExecuteThreadMap.get(resourceId));
                }
            }
        }

        List<String> updateScenarioReportList = new ArrayList<>();
        for (Map.Entry<String, String> entry : apiScenarioCaseExecInfo.entrySet()) {
            String resourceId = entry.getKey();
            String executeResult = entry.getValue();
            if (StringUtils.equalsIgnoreCase(executeResult, TestPlanApiExecuteStatus.RUNNING.name())) {
                apiScenarioCaseExecInfo.put(resourceId, TestPlanApiExecuteStatus.FAILD.name());
                updateScenarioReportList.add(apiScenarioThreadMap.get(resourceId));
                if (StringUtils.isNotEmpty(apiScenarioThreadMap.get(resourceId))) {
                    JmeterThreadUtils.stop(apiScenarioThreadMap.get(resourceId));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(updateScenarioReportList)) {
            ApiScenarioReportMapper apiScenarioReportMapper = CommonBeanFactory.getBean(ApiScenarioReportMapper.class);
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(updateScenarioReportList).andStatusEqualTo("Running");
            ApiScenarioReport report = new ApiScenarioReport();
            report.setStatus("Error");
            apiScenarioReportMapper.updateByExampleSelective(report, example);
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
        }

        this.countUnFinishedNum();
    }

    public synchronized void updateReport(Map<String, String> apiCaseExecResultInfo, Map<String, String> apiScenarioCaseExecResultInfo) {
        if (MapUtils.isNotEmpty(apiCaseExecResultInfo)) {
            this.apiCaseReportMap.putAll(apiCaseExecResultInfo);
        }

        if (MapUtils.isNotEmpty(apiScenarioCaseExecResultInfo)) {
            this.apiScenarioReportMap.putAll(apiScenarioCaseExecResultInfo);
        }
    }

    public Map<String, String> getRunningApiCaseReportMap() {
        //key: reportId, value: testPlanApiCaseId
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : apiCaseExecInfo.entrySet()) {
            String planCaseId = entry.getKey();
            String status = entry.getValue();
            if (StringUtils.equalsIgnoreCase(status, TestPlanApiExecuteStatus.RUNNING.name())) {
                if (apiCaseExecuteThreadMap.containsKey(planCaseId)) {
                    returnMap.put(apiCaseExecuteThreadMap.get(planCaseId), planCaseId);
                }
            }
        }
        return returnMap;
    }

    public Map<String, String> getRunningScenarioReportMap() {
        //key: reportId, value: testPlanApiScenarioId
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : apiScenarioCaseExecInfo.entrySet()) {
            String planScenarioId = entry.getKey();
            String status = entry.getValue();
            if (StringUtils.equalsIgnoreCase(status, TestPlanApiExecuteStatus.RUNNING.name())) {
                if (apiScenarioThreadMap.containsKey(planScenarioId)) {
                    returnMap.put(apiScenarioThreadMap.get(planScenarioId), planScenarioId);
                }
            }
        }
        return returnMap;
    }
}