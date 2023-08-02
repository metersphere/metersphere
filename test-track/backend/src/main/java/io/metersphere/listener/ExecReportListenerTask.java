package io.metersphere.listener;

import io.metersphere.base.domain.ApiExecutionQueue;
import io.metersphere.base.domain.ApiExecutionQueueDetailExample;
import io.metersphere.base.domain.ApiExecutionQueueExample;
import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.commons.constants.TestPlanExecuteCaseType;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.plan.service.AutomationCaseExecOverService;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class ExecReportListenerTask implements Runnable {
    private ConsumerRecord<?, String> record;

    protected ApiExecutionQueueMapper apiExecutionQueueMapper;
    private ApiExecutionQueueDetailMapper apiExecutionQueueDetailMapper;
    private TestPlanReportService testPlanReportService;
    private AutomationCaseExecOverService automationCaseExecOverService;
    private RedisTemplateService redisTemplateService;

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
        if (CollectionUtils.isEmpty(queues) && this.isTestPlanIsEmptyInRedis(testPlanReportId)) {
            LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
            testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
        } else if (CollectionUtils.isNotEmpty(queues)) {
            List<String> ids = queues.stream().map(ApiExecutionQueue::getId).collect(Collectors.toList());
            ApiExecutionQueueDetailExample detailExample = new ApiExecutionQueueDetailExample();
            detailExample.createCriteria().andQueueIdIn(ids);
            long count = apiExecutionQueueDetailMapper.countByExample(detailExample);
            if (count == 0 && this.isTestPlanIsEmptyInRedis(testPlanReportId)) {
                LoggerUtil.info("Normal execution completes, update test plan report status：" + testPlanReportId);
                testPlanReportService.testPlanExecuteOver(testPlanReportId, TestPlanReportStatus.COMPLETED.name());
                LoggerUtil.info("Clear Queue：" + ids);
                ApiExecutionQueueExample queueExample = new ApiExecutionQueueExample();
                queueExample.createCriteria().andIdIn(ids);
                apiExecutionQueueMapper.deleteByExample(queueExample);
            }
        }
    }

    /**
     * 测试计划执行时会将运行标志放入redis中。当测试计划执行队列入库后，会将redis中的标志清除。
     */
    private boolean isTestPlanIsEmptyInRedis(String testPlanReportId) {
        Object scenarioObj = redisTemplateService.get(testPlanReportId + TestPlanExecuteCaseType.SCENARIO);
        Object apiCaseObj = redisTemplateService.get(testPlanReportId + TestPlanExecuteCaseType.API_CASE);
        Object uiObj = redisTemplateService.get(testPlanReportId + TestPlanExecuteCaseType.UI_SCENARIO);
        Object loadObj = redisTemplateService.get(testPlanReportId + TestPlanExecuteCaseType.LOAD_CASE);
        return ObjectUtils.isEmpty(scenarioObj) && ObjectUtils.isEmpty(apiCaseObj) && ObjectUtils.isEmpty(uiObj) && ObjectUtils.isEmpty(loadObj);
    }
}
