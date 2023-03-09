package io.metersphere.plan.notice;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.TestPlanLoadCaseExample;
import io.metersphere.base.domain.TestPlanLoadCaseWithBLOBs;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.consumer.LoadTestFinishEvent;
import io.metersphere.plan.service.PerfQueueService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class LoadReportStatusEvent implements LoadTestFinishEvent {

    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

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
            // 更新测试计划关联数据状态
            TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
            example.createCriteria().andIdEqualTo(loadTestReport.getTestId());
            if (testPlanLoadCaseMapper.countByExample(example) > 0) {
                TestPlanLoadCaseWithBLOBs loadCase = new TestPlanLoadCaseWithBLOBs();
                loadCase.setId(loadTestReport.getTestId());
                loadCase.setStatus(TestPlanLoadCaseStatus.success.name());
                testPlanLoadCaseMapper.updateByPrimaryKeySelective(loadCase);
                LogUtil.info("Execute test_plan_load_case OVER.  Now send kafka to Test_Track. key:" + loadCase.getId());
                PerfQueueService perfQueueService = CommonBeanFactory.getBean(PerfQueueService.class);
                if (perfQueueService != null) {
                    perfQueueService.checkTestPlanLoadCaseExecOver(loadCase.getId(), null);
                }
            }
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
