package io.metersphere.listener;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.utils.LoggerUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        LoggerUtil.info("Execute message received：", record.value());
        this.testPlanReportTestEnded(record.value());
    }

    public void testPlanReportTestEnded(String testPlanReportId) {
        // 检查测试计划中其他队列是否结束
        ApiExecutionQueueExample executionQueueExample = new ApiExecutionQueueExample();
        executionQueueExample.createCriteria().andReportIdEqualTo(testPlanReportId);
        List<ApiExecutionQueue> queues = queueMapper.selectByExample(executionQueueExample);
        if (CollectionUtils.isEmpty(queues)) {
            LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
            testPlanReportService.finishedTestPlanReport(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
        } else {
            List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
            ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
            detailExample.createCriteria().andQueueIdIn(ids);
            long count = executionQueueDetailMapper.countByExample(detailExample);
            if (count == 0) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                testPlanReportService.finishedTestPlanReport(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
                LoggerUtil.info("Clear Queue：" + ids);
                ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
                queueExample.createCriteria().andIdIn(ids);
                queueMapper.deleteByExample(queueExample);
            }
        }
    }
}
