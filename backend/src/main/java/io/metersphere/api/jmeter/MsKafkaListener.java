package io.metersphere.api.jmeter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.exec.utils.NamedThreadFactory;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.config.KafkaConfig;
import io.metersphere.utils.LoggerUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.Resource;
import java.util.List;
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
            records.forEach(item -> {
                LoggerUtil.info("接收到报告【" + item.key() + "】，加入到结果处理队列");
                KafkaListenerTask task = new KafkaListenerTask();
                task.setApiExecutionQueueService(apiExecutionQueueService);
                task.setTestResultService(testResultService);
                task.setMapper(mapper);
                task.setRecord(item);
                threadPool.execute(task);
                this.outKafkaPoolLogger();
            });
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        } finally {
            ack.acknowledge();
        }
    }

    public void outKafkaPoolLogger() {
        StringBuffer buffer = new StringBuffer()
                .append("\n")
                .append("KAFKA Consume 线程池详情：")
                .append("\n")
                .append(" KAFKA Consume 核心线程数：" + threadPool.getCorePoolSize())
                .append("\n")
                .append(" KAFKA Consume 活动线程数：" + threadPool.getActiveCount())
                .append("\n")
                .append(" KAFKA Consume 最大线程数：" + threadPool.getMaximumPoolSize())
                .append("\n")
                .append(" KAFKA Consume 最大队列数：" + (threadPool.getQueue().size() + threadPool.getQueue().remainingCapacity()))
                .append("\n")
                .append(" KAFKA Consume 当前排队线程数：" + (threadPool.getQueue().size()))
                .append("\n");
        LoggerUtil.info(buffer.toString());
    }

}
