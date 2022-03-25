package io.metersphere.api.cache;

import io.metersphere.commons.constants.TestPlanApiExecuteStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/8/20 3:29 下午
 */
public class TestPlanReportExecuteCatch {
    private static Logger testPlanLog = LoggerFactory.getLogger("testPlanExecuteLog");
    private static Map<String, TestPlanExecuteInfo> testPlanReportMap = new HashMap<>();

    private TestPlanReportExecuteCatch() {
    }

    public synchronized static void addApiTestPlanExecuteInfo(String reportId, String creator,
                                                              Map<String, String> apiCaseExecInfo, Map<String, String> apiScenarioCaseExecInfo, Map<String, String> loadCaseExecInfo) {
        if (testPlanReportMap == null) {
            testPlanReportMap = new HashMap<>();
        }
        if (apiCaseExecInfo == null) {
            apiCaseExecInfo = new HashMap<>();
        }
        if (apiScenarioCaseExecInfo == null) {
            apiScenarioCaseExecInfo = new HashMap<>();
        }
        if (loadCaseExecInfo == null) {
            loadCaseExecInfo = new HashMap<>();
        }

        TestPlanExecuteInfo executeInfo = new TestPlanExecuteInfo(reportId, creator);
        executeInfo.setApiCaseExecInfo(apiCaseExecInfo);
        executeInfo.setApiScenarioCaseExecInfo(apiScenarioCaseExecInfo);
        executeInfo.setLoadCaseExecInfo(loadCaseExecInfo);
        testPlanReportMap.put(reportId, executeInfo);
    }

    public synchronized static String getCreator(String reportId) {
        if (testPlanReportMap != null && testPlanReportMap.containsKey(reportId)) {
            return testPlanReportMap.get(reportId).getCreator();
        } else {
            return null;
        }
    }

    public synchronized static boolean containsReport(String reportId) {
        if(StringUtils.isEmpty(reportId)){
            return false;
        }else {
            return testPlanReportMap != null && testPlanReportMap.containsKey(reportId);
        }
    }

    public synchronized static void updateApiTestPlanExecuteInfo(String reportId,
                                                                 Map<String, String> apiCaseExecInfo, Map<String, String> apiScenarioCaseExecInfo, Map<String, String> loadCaseExecInfo) {
        if (testPlanReportMap != null && testPlanReportMap.containsKey(reportId)) {
            testPlanReportMap.get(reportId).updateExecuteInfo(apiCaseExecInfo, apiScenarioCaseExecInfo, loadCaseExecInfo);
        }
    }

    public synchronized static void updateTestPlanThreadInfo(String reportId,
                                                             Map<String, String> apiCaseExecResultInfo, Map<String, String> apiScenarioCaseExecResultInfo, Map<String, String> loadCaseExecResultInfo) {
        if (testPlanReportMap != null && testPlanReportMap.containsKey(reportId)) {
            testPlanReportMap.get(reportId).updateThreadResult(apiCaseExecResultInfo, apiScenarioCaseExecResultInfo, loadCaseExecResultInfo);
        }
    }

    public synchronized static void updateTestPlanReport(String reportId,
                                                         Map<String, String> apiCaseExecResultInfo, Map<String, String> apiScenarioCaseExecResultInfo) {
        if (testPlanReportMap != null && testPlanReportMap.containsKey(reportId)) {
            testPlanReportMap.get(reportId).updateReport(apiCaseExecResultInfo, apiScenarioCaseExecResultInfo);
        }
    }


    public static TestPlanExecuteInfo getTestPlanExecuteInfo(String reportId) {
        return testPlanReportMap.get(reportId);
    }

    public static synchronized void setReportDataCheckResult(String reportId, boolean result) {
        if (testPlanReportMap.containsKey(reportId)) {
            testPlanReportMap.get(reportId).setReportDataInDataBase(result);
        }
    }

    public static synchronized void remove(String reportId) {
        if (testPlanReportMap.containsKey(reportId)) {
            testPlanReportMap.get(reportId).finishAllTask();
            testPlanReportMap.remove(reportId);
        }
    }

    public static void finishAllTask(String planReportId) {
        testPlanLog.info("ReportId[" + planReportId + "] finish task!");
        if (testPlanReportMap.containsKey(planReportId)) {
            testPlanReportMap.get(planReportId).finishAllTask();
        }
    }

    public static void set(String planReportId, List<String> executeErrorList) {
        if (TestPlanReportExecuteCatch.containsReport(planReportId)) {
            if (!executeErrorList.isEmpty()) {
                Map<String, String> executeErrorMap = new HashMap<>();
                for (String id : executeErrorList) {
                    executeErrorMap.put(id, TestPlanApiExecuteStatus.FAILD.name());
                }
                TestPlanReportExecuteCatch.updateApiTestPlanExecuteInfo(planReportId, executeErrorMap, null, null);
            }
        }
    }
}
