package io.metersphere.track.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.consumer.LoadTestFinishEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class LoadReportStatusEvent implements LoadTestFinishEvent {

    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;

    private void updateLoadCaseStatus(LoadTestReport loadTestReport) {
        String reportId = loadTestReport.getId();
        String status = loadTestReport.getStatus();
        if (StringUtils.isNotBlank(reportId)) {
            String result = "";
            if (StringUtils.equals(PerformanceTestStatus.Error.name(), status)) {
                result = "error";
            }
            if (StringUtils.equals(PerformanceTestStatus.Completed.name(), status)) {
                result = "success";
            }
            extTestPlanLoadCaseMapper.updateCaseStatus(reportId, result);
        }
    }

    @Override
    public void execute(LoadTestReport loadTestReport) {
        if (StringUtils.equals(ReportTriggerMode.CASE.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.TEST_PLAN_SCHEDULE.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.TEST_PLAN_API.name(), loadTestReport.getTriggerMode())) {
            if (StringUtils.equalsAny(loadTestReport.getStatus(),
                    PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                updateLoadCaseStatus(loadTestReport);
            }
        }
    }
}
