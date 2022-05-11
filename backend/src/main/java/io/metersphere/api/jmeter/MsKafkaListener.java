package io.metersphere.api.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.exec.utils.NamedThreadFactory;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.config.KafkaConfig;
import io.metersphere.utils.LoggerUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MsKafkaListener {
    public static final String CONSUME_ID = "ms-api-exec-consume";
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private TestResultService testResultService;
    @Resource
    private ObjectMapper mapper;

    private static final Map<String, String> RUN_MODE_MAP = new HashMap<String, String>() {{
        this.put(ApiRunMode.SCHEDULE_API_PLAN.name(), "schedule-task");
        this.put(ApiRunMode.JENKINS_API_PLAN.name(), "schedule-task");
        this.put(ApiRunMode.MANUAL_PLAN.name(), "schedule-task");

        this.put(ApiRunMode.DEFINITION.name(), "api-test-case-task");
        this.put(ApiRunMode.JENKINS.name(), "api-test-case-task");
        this.put(ApiRunMode.API_PLAN.name(), "api-test-case-task");
        this.put(ApiRunMode.JENKINS_API_PLAN.name(), "api-test-case-task");
        this.put(ApiRunMode.MANUAL_PLAN.name(), "api-test-case-task");


        this.put(ApiRunMode.SCENARIO.name(), "api-scenario-task");
        this.put(ApiRunMode.SCENARIO_PLAN.name(), "api-scenario-task");
        this.put(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), "api-scenario-task");
        this.put(ApiRunMode.SCHEDULE_SCENARIO.name(), "api-scenario-task");
        this.put(ApiRunMode.JENKINS_SCENARIO_PLAN.name(), "api-scenario-task");

    }};

    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 20;
    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 20;
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
            new NamedThreadFactory("MS-KAFKA-LISTENER-TASK"));

    @KafkaListener(id = CONSUME_ID, topics = KafkaConfig.TOPICS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "batchFactory")
    public void consume(List<ConsumerRecord<?, String>> records, Acknowledgment ack) {
        try {
            KafkaListenerTask task = new KafkaListenerTask();
            task.setApiExecutionQueueService(apiExecutionQueueService);
            task.setTestResultService(testResultService);
            task.setMapper(mapper);
            task.setRecords(records);
            threadPool.execute(task);
            this.outApiThreadPoolExecutorLogger();
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        } finally {
            ack.acknowledge();
        }
    }

    public void outApiThreadPoolExecutorLogger() {
        StringBuffer buffer = new StringBuffer("KAFKA 消费队列处理详情 ");
        buffer.append("\n").append("KAFKA Consume 线程池详情：").append("\n");
        buffer.append(" KAFKA Consume 核心线程数：" + threadPool.getCorePoolSize()).append("\n");
        buffer.append(" KAFKA Consume 活动线程数：" + threadPool.getActiveCount()).append("\n");
        buffer.append(" KAFKA Consume 最大线程数：" + threadPool.getMaximumPoolSize()).append("\n");
        buffer.append(" KAFKA Consume 最大队列数：" + (threadPool.getQueue().size() + threadPool.getQueue().remainingCapacity())).append("\n");
        buffer.append(" KAFKA Consume 当前排队线程数：" + (threadPool.getQueue().size())).append("\n");
        LoggerUtil.info(buffer.toString());
    }

}
