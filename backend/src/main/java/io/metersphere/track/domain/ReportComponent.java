package io.metersphere.track.domain;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import org.apache.commons.lang3.StringUtils;

public abstract class ReportComponent {
    protected String componentId;
    protected TestPlanDTO testPlan;

    public ReportComponent(TestPlanDTO testPlan) {
        this.testPlan = testPlan;
    }

    public abstract void readRecord(TestPlanCaseDTO testCase);

    public abstract void afterBuild(TestCaseReportMetricDTO testCaseReportMetric);

    public void readRecord(TestPlanApiCaseDTO testCase) {
    }

    public void readRecord(ApiScenarioDTO testCase) {
    }

    public void readRecord(TestPlanLoadCaseDTO testCase) {
    }

}
