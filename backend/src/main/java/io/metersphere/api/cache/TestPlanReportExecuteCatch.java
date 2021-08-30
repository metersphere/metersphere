package io.metersphere.api.cache;

import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.base.domain.ApiDefinitionExecResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/20 3:29 下午
 */
public class TestPlanReportExecuteCatch {
    private static Map<String, TestPlanExecuteInfo> testPlanReportMap = new HashMap<>();

    private TestPlanReportExecuteCatch() {
    }

    public synchronized static void addApiTestPlanExecuteInfo(String reportId,
              Map<String, String> apiCaseExecInfo, Map<String, String> apiScenarioCaseExecInfo, Map<String, String> loadCaseExecInfo) {
        if(testPlanReportMap == null){
            testPlanReportMap = new HashMap<>();
        }
        if(apiCaseExecInfo == null){
            apiCaseExecInfo = new HashMap<>();
        }
        if(apiScenarioCaseExecInfo == null){
            apiScenarioCaseExecInfo = new HashMap<>();
        }
        if(loadCaseExecInfo == null){
            loadCaseExecInfo = new HashMap<>();
        }

        TestPlanExecuteInfo executeInfo = new TestPlanExecuteInfo();
        executeInfo.setReportId(reportId);
        executeInfo.setApiCaseExecInfo(apiCaseExecInfo);
        executeInfo.setApiScenarioCaseExecInfo(apiScenarioCaseExecInfo);
        executeInfo.setLoadCaseExecInfo(loadCaseExecInfo);
        testPlanReportMap.put(reportId,executeInfo);
    }

    public synchronized static void updateApiTestPlanExecuteInfo(String reportId,
                                                                 Map<String, String> apiCaseExecInfo, Map<String, String> apiScenarioCaseExecInfo, Map<String, String> loadCaseExecInfo) {
        if(testPlanReportMap != null && testPlanReportMap.containsKey(reportId)){
            testPlanReportMap.get(reportId).updateExecuteInfo(apiCaseExecInfo,apiScenarioCaseExecInfo,loadCaseExecInfo);
        }
    }

    public synchronized static void updateTestPlanExecuteResultInfo(String reportId,
                                                                 Map<String, ApiDefinitionExecResult> apiCaseExecResultInfo, Map<String, APIScenarioReportResult> apiScenarioCaseExecResultInfo, Map<String, String> loadCaseExecResultInfo) {
        if(testPlanReportMap != null && testPlanReportMap.containsKey(reportId)){
            testPlanReportMap.get(reportId).updateExecuteResult(apiCaseExecResultInfo,apiScenarioCaseExecResultInfo,loadCaseExecResultInfo);
        }
    }


    public static TestPlanExecuteInfo getTestPlanExecuteInfo(String reportId){
        return testPlanReportMap.get(reportId);
    }

    public static synchronized void setReportDataCheckResult(String reportId, boolean result) {
        if(testPlanReportMap.containsKey(reportId)){
            testPlanReportMap.get(reportId).setReportDataInDataBase(result);
        }
    }

    public static synchronized void remove(String reportId){
        if(testPlanReportMap.containsKey(reportId)){
            testPlanReportMap.remove(reportId);
        }
    }

    public static void finishAllTask(String planReportId) {
        if(testPlanReportMap.containsKey(planReportId)){
            testPlanReportMap.get(planReportId).finishAllTask();
        }
    }
}
