package io.metersphere.plan.notice;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.consumer.LoadTestFinishEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
@Transactional(rollbackFor = Exception.class)
public class LoadReportStatusEvent implements LoadTestFinishEvent {

    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;

    private void updateLoadCaseStatus(LoadTestReport loadTestReport) {
        String reportId = loadTestReport.getId();
        String status = loadTestReport.getStatus();
        if (StringUtils.isNotBlank(reportId)) {
            String result = StringUtils.EMPTY;
            if (StringUtils.equals(PerformanceTestStatus.Error.name(), status)) {
                result = TestPlanLoadCaseStatus.error.name();
            }
            if (StringUtils.equals(PerformanceTestStatus.Completed.name(), status)) {
                result = TestPlanLoadCaseStatus.success.name();
            }
            LogUtil.info("update plan load case status: " + result);
            extTestPlanLoadCaseMapper.updateCaseStatus(reportId, result);
        }
    }

    @Override
    public void execute(LoadTestReport loadTestReport) {
        if (StringUtils.equals(ReportTriggerMode.MANUAL.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.BATCH.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.TEST_PLAN_SCHEDULE.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.TEST_PLAN_API.name(), loadTestReport.getTriggerMode())) {
            if (StringUtils.equalsAny(loadTestReport.getStatus(),
                    PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                updateLoadCaseStatus(loadTestReport);
            }
        }
    }
}
