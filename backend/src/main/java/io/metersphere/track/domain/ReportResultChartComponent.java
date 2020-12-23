package io.metersphere.track.domain;

import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportResultChartComponent extends ReportComponent {
    Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap = new HashMap<>();

    public ReportResultChartComponent(TestPlanDTO testPlan) {
        super(testPlan);
        componentId = "3";
    }


    @Override
    public void readRecord(TestPlanCaseDTO testCase) {
        getStatusResultMap(reportStatusResultMap, testCase);
    }

    @Override
    public void afterBuild(TestCaseReportMetricDTO testCaseReportMetric) {
//        testCaseReportMetric.setExecuteResult(getReportStatusResult());
    }

    private List<TestCaseReportStatusResultDTO> getReportStatusResult() {
        List<TestCaseReportStatusResultDTO> reportStatusResult = new ArrayList<>();
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Pass.name());
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Failure.name());
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Blocking.name());
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Skip.name());
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Underway.name());
        addToReportStatusResultList(reportStatusResult, TestPlanTestCaseStatus.Prepare.name());
        return reportStatusResult;
    }

    private void addToReportStatusResultList(List<TestCaseReportStatusResultDTO> reportStatusResultList, String status) {
        if (reportStatusResultMap.get(status) != null) {
            reportStatusResultList.add(reportStatusResultMap.get(status));
        }
    }

    private void getStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, TestPlanCaseDTO testCase) {
        TestCaseReportStatusResultDTO statusResult = reportStatusResultMap.get(testCase.getStatus());
        if (statusResult == null) {
            statusResult = new TestCaseReportStatusResultDTO();
            statusResult.setStatus(testCase.getStatus());
            statusResult.setCount(0);
        }
        statusResult.setCount(statusResult.getCount() + 1);
        reportStatusResultMap.put(testCase.getStatus(), statusResult);
    }
}
