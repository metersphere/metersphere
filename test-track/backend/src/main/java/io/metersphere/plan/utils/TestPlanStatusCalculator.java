package io.metersphere.plan.utils;

import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.plan.constant.ApiReportStatus;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.plan.dto.TestPlanSimpleReportDTO;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class TestPlanStatusCalculator {


    public static void buildStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, String result) {
        // unexecute 存在于 ui 执行结果中
        if (StringUtils.isBlank(result) || StringUtils.equals("UnExecute", result)) {
            result = TestPlanTestCaseStatus.Prepare.name();
        }
        TestCaseReportStatusResultDTO statusResult = reportStatusResultMap.get(result);
        if (statusResult == null) {
            statusResult = new TestCaseReportStatusResultDTO();
            statusResult.setStatus(result);
            statusResult.setCount(0);
        }
        statusResult.setCount(statusResult.getCount() + 1);
        reportStatusResultMap.put(result, statusResult);
    }

    public static void addToReportStatusResultList(Map<String, TestCaseReportStatusResultDTO> resultMap,
                                                   List<TestCaseReportStatusResultDTO> reportStatusResultList, String status) {
        if (resultMap.get(status) != null) {
            reportStatusResultList.add(resultMap.get(status));
        } else if (StringUtils.isBlank(status) && resultMap.get(TestPlanTestCaseStatus.Prepare.name()) != null) {
            reportStatusResultList.add(resultMap.get(TestPlanTestCaseStatus.Prepare.name()));
        }
    }

    /**
     * 将map转成前端需要的数组数据
     * @param resultMap
     * @param statusResult
     */
    public static void addToReportCommonStatusResultList(Map<String, TestCaseReportStatusResultDTO> resultMap,
                                                        List<TestCaseReportStatusResultDTO> statusResult) {
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(resultMap, statusResult, "error");
        addToReportStatusResultList(resultMap, statusResult, "Error");
        addToReportStatusResultList(resultMap, statusResult, "run");
        addToReportStatusResultList(resultMap, statusResult, "Fail");
        addToReportStatusResultList(resultMap, statusResult, "success");
        addToReportStatusResultList(resultMap, statusResult, "Success");
        addToReportStatusResultList(resultMap, statusResult, "STOP");
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Prepare.name());
        addToReportStatusResultList(resultMap, statusResult, ExecuteResult.ERROR_REPORT_RESULT.toString());
        for (ApiReportStatus value : ApiReportStatus.values()) {
            addToReportStatusResultList(resultMap, statusResult, value.name());
        }
    }

    /**
     * 将当前用例状态对应的统计数据存储在map中
     */
    public static void buildStatusResultMap(List<PlanReportCaseDTO> planReportCaseDTOS,
                                            Map<String, TestCaseReportStatusResultDTO> statusResultMap,
                                            TestPlanSimpleReportDTO report, String successStatus) {
        planReportCaseDTOS.forEach(item -> {
            report.setCaseCount((report.getCaseCount() == null ? 0 : report.getCaseCount()) + 1);
            String status = item.getStatus();
            if (StringUtils.isNotBlank(status)
                    && !StringUtils.equalsAnyIgnoreCase(status, TestPlanTestCaseStatus.Underway.name(), TestPlanTestCaseStatus.Prepare.name(),
                    ExecuteResult.UN_EXECUTE.getValue(), ExecuteResult.STOP.getValue(), ApiReportStatus.PENDING.name(), ApiReportStatus.STOPPED.name())) {
                // 计算执行过的数量
                report.setExecuteCount(report.getExecuteCount() + 1);
                if (StringUtils.equalsIgnoreCase(successStatus, status)) {
                    // 计算执行成功的数量
                    report.setPassCount(report.getPassCount() + 1);
                }
            }
            buildStatusResultMap(statusResultMap, status);
        });
    }
}
