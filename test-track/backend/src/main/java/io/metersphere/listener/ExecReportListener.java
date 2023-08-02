package io.metersphere.listener;

import io.metersphere.base.mapper.ApiExecutionQueueDetailMapper;
import io.metersphere.base.mapper.ApiExecutionQueueMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.plan.service.AutomationCaseExecOverService;
import io.metersphere.plan.service.TestPlanReportService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.NamedThreadFactory;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private RedisTemplateService redisTemplateService;
    @Resource
    private AutomationCaseExecOverService automationCaseExecOverService;

    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 5;
    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 5;
    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 1;
    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 10000;
    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(WORK_QUEUE_SIZE),
            new NamedThreadFactory("MS-TEST-PLAN-CASE-EXEC-LISTENER-TASK"));

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_REPORT_TOPIC, groupId = "${spring.application.name}", containerFactory = "batchFactory")
    public void consume(List<ConsumerRecord<?, String>> records, Acknowledgment ack) {

        try {
            records.forEach(item -> {
                LoggerUtil.info("接收到报告【key:" + item.key() + ",value:" + item.value() + "】，加入到结果处理队列");
                ExecReportListenerTask task = new ExecReportListenerTask();
                task.setApiExecutionQueueMapper(queueMapper);
                task.setApiExecutionQueueDetailMapper(executionQueueDetailMapper);
                task.setAutomationCaseExecOverService(automationCaseExecOverService);
                task.setTestPlanReportService(testPlanReportService);
                task.setRedisTemplateService(redisTemplateService);
                task.setRecord(item);
                threadPool.execute(task);
            });
        } catch (Exception e) {
            LoggerUtil.error("测试计划自动化用例结束后-KAFKA消费失败：", e);
        } finally {
            ack.acknowledge();
        }
    }
}
