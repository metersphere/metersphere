package io.metersphere.listener;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.plan.service.AutomationCaseExecOverService;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExecReportListenerTask implements Runnable {
    private ConsumerRecord<?, String> record;

    protected ApiExecutionQueueMapper apiExecutionQueueMapper;
    private ApiExecutionQueueDetailMapper apiExecutionQueueDetailMapper;
    private TestPlanReportService testPlanReportService;
    private AutomationCaseExecOverService automationCaseExecOverService;


    @Override
    public void run() {
        Object testIdObj = record.key();
        if (ObjectUtils.isEmpty(testIdObj)) {
            LoggerUtil.info("Execute message. received：", record.value());
            this.testPlanReportTestEnded(record.value());
        } else {
            LoggerUtil.info("Execute message. key:[" + testIdObj.toString() + "], received：", record.value());
            this.automationCaseTestEnd(testIdObj.toString());
        }
    }


    public void automationCaseTestEnd(String testId) {
        //自动化用例执行完成之后的后续操作
        automationCaseExecOverService.automationCaseExecOver(testId);
    }

    public void testPlanReportTestEnded(String testPlanReportId) {
        // 检查测试计划中其他队列是否结束
        ApiExecutionQueueExample executionQueueExample = new ApiExecutionQueueExample();
        executionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
        List<ApiExecutionQueue> queues = apiExecutionQueueMapper.selectByExample(executionQueueExample);
        if (CollectionUtils.isEmpty(queues)) {
            LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
            testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
        } else {
            List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
            ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
            detailExample.createCriteria().andQueueIdIn(ids);
            long count = apiExecutionQueueDetailMapper.countByExample(detailExample);
            if (count == 0) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
                LoggerUtil.info("Clear Queue：" + ids);
                ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
                queueExample.createCriteria().andIdIn(ids);
                apiExecutionQueueMapper.deleteByExample(queueExample);
            }
        }
    }
}
