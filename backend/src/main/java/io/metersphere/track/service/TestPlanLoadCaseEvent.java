package io.metersphere.track.service;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.consumer.LoadTestFinishEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author song.tianyang
 * @Date 2021/1/13 2:53 下午
 * @Description
 */
@Service
public class TestPlanLoadCaseEvent implements LoadTestFinishEvent {
    @Resource
    TestPlanReportService testPlanReportService;

    @Override
    public void execute(LoadTestReport loadTestReport) {
        if (StringUtils.equals(NoticeConstants.Mode.SCHEDULE, loadTestReport.getTriggerMode()) ) {
            if (StringUtils.equalsAny(loadTestReport.getStatus(),
                    PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                testPlanReportService.updatePerformanceTestStatus(loadTestReport.getId());
            }
        }
    }
}
