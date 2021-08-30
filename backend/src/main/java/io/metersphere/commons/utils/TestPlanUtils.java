package io.metersphere.commons.utils;

import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.track.dto.TestPlanSimpleReportDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class TestPlanUtils {


    public static void getStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, String result) {
        if (StringUtils.isBlank(result)) {
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

    public static void addToReportCommonStatusResultList(Map<String, TestCaseReportStatusResultDTO> resultMap,
                                     List<TestCaseReportStatusResultDTO> statusResult) {
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(resultMap, statusResult, "error");
        addToReportStatusResultList(resultMap, statusResult, "run");
        addToReportStatusResultList(resultMap, statusResult, "Fail");
        addToReportStatusResultList(resultMap, statusResult, "success");
        addToReportStatusResultList(resultMap, statusResult, "Success");
        addToReportStatusResultList(resultMap, statusResult, TestPlanTestCaseStatus.Prepare.name());
    }

    public static void calculatePlanReport(List<PlanReportCaseDTO> planReportCaseDTOS,
                                                   Map<String, TestCaseReportStatusResultDTO> statusResultMap,
                                                   TestPlanSimpleReportDTO report, String successStatus) {
        planReportCaseDTOS.forEach(item -> {
            report.setCaseCount((report.getCaseCount() == null ? 0 : report.getCaseCount()) + 1);
            String status = item.getStatus();
            if (StringUtils.isNotBlank(status)
                    && !StringUtils.equalsAny(status, TestPlanTestCaseStatus.Underway.name(), TestPlanTestCaseStatus.Prepare.name())) {
                report.setExecuteCount(report.getExecuteCount() + 1);
                if (StringUtils.equals(successStatus, status)) {
                    report.setPassCount(report.getPassCount() + 1);
                }
            }else {
                System.out.println(status);
            }
            TestPlanUtils.getStatusResultMap(statusResultMap, status);
        });
    }
}
