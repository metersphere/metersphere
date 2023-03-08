package io.metersphere.listener;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.plan.service.TestCaseSyncStatusService;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExecReportListener {
    public static final String CONSUME_ID = "exec-test-plan-report";
    @Resource
    protected ApiExecutionQueueMapper queueMapper;
    @Resource
    private ApiExecutionQueueDetailMapper executionQueueDetailMapper;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private TestCaseSyncStatusService testCaseSyncStatusService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        Object testIdObj = record.key();
        if (ObjectUtils.isEmpty(testIdObj)) {
            LoggerUtil.info("Execute message. received：", record.value());
            this.testPlanReportTestEnded(record.value());
        } else {
            LoggerUtil.info("Execute message. key:[" + testIdObj.toString() + "], received：", record.value());
            this.automationCaseTestEnd(testIdObj.toString());
        }

    }

    /**
     * 测试计划相关的自动化用例结束后，会根据测试计划的配置判断是否要同步功能用例的状态
     * 目前暂时只有这一个需求。后续如果有了更多操作，建议将该方法内的逻辑处理放入一个共有方法中。
     *
     * @param testId
     */
    public void automationCaseTestEnd(String testId) {
        testCaseSyncStatusService.checkAndUpdateFunctionCaseStatus(testId);
    }

    public void testPlanReportTestEnded(String testPlanReportId) {
        // 检查测试计划中其他队列是否结束
        ApiExecutionQueueExample executionQueueExample = new ApiExecutionQueueExample();
        executionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
        List<ApiExecutionQueue> queues = queueMapper.selectByExample(executionQueueExample);
        if (CollectionUtils.isEmpty(queues)) {
            LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
            testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
        } else {
            List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
            ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
            detailExample.createCriteria().andQueueIdIn(ids);
            long count = executionQueueDetailMapper.countByExample(detailExample);
            if (count == 0) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
                LoggerUtil.info("Clear Queue：" + ids);
                ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
                queueExample.createCriteria().andIdIn(ids);
                queueMapper.deleteByExample(queueExample);
            }
        }
    }
}
