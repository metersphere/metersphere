package io.metersphere.track.domain;

import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;

public abstract class ReportComponent {
    protected String componentId;
    protected TestPlanDTO testPlan;

    public ReportComponent(TestPlanDTO testPlan) {
        this.testPlan = testPlan;
    }

    public abstract void readRecord(TestPlanCaseDTO testCase);

    public abstract void afterBuild(TestCaseReportMetricDTO testCaseReportMetric);
}
