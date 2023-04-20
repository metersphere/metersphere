package io.metersphere.api.jmeter;

import io.metersphere.api.dto.MsgDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.RequestResult;
import io.metersphere.service.ApiExecutionQueueService;
import io.metersphere.service.RedisTemplateService;
import io.metersphere.service.TestResultService;
import io.metersphere.service.definition.ApiDefinitionEnvService;
import io.metersphere.utils.LoggerUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MsKafkaListener {
    public static final String CONSUME_ID = "ms-api-exec-consume";
    public static final String DEBUG_CONSUME_ID = "ms-api-debug-consume";
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;
    @Resource
    private TestResultService testResultService;
    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 10;
    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 10;
    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 1;
    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 10000;

    @Resource
    private RedisTemplateService redisTemplateService;

    @Resource
    private ApiDefinitionEnvService apiDefinitionEnvService;

    private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(WORK_QUEUE_SIZE),
            new NamedThreadFactory("MS-KAFKA-LISTENER-TASK"));

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.API_REPORT_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "batchFactory")
    public void consume(List<ConsumerRecord<?, String>> records, Acknowledgment ack) {
        try {
            records.forEach(item -> {
                LoggerUtil.info("接收到报告【" + item.key() + "】，加入到结果处理队列");
                KafkaListenerTask task = new KafkaListenerTask();
                task.setApiExecutionQueueService(apiExecutionQueueService);
                task.setTestResultService(testResultService);
                task.setRecord(item);
                task.setRedisTemplateService(redisTemplateService);
                threadPool.execute(task);
            });
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        } finally {
            ack.acknowledge();
        }
    }

    @KafkaListener(id = DEBUG_CONSUME_ID, topics = KafkaTopicConstants.DEBUG_TOPICS, groupId = "${spring.kafka.consumer.debug.group-id}")
    public void debugConsume(ConsumerRecord<?, String> record) {
        try {
            LoggerUtil.info("接收到执行结果：", record.key());
            if (ObjectUtils.isNotEmpty(record.value()) && WebSocketUtil.has(record.key().toString())) {
                MsgDTO dto = JSONUtil.parseObject(record.value(), MsgDTO.class);
                if (StringUtils.isNotBlank(dto.getContent()) && dto.getContent().startsWith("result_")) {
                    String content = dto.getContent().substring(7);
                    if (StringUtils.isNotBlank(content)) {
                        RequestResult baseResult = JSONUtil.parseObject(content, RequestResult.class);
                        if (ObjectUtils.isNotEmpty(baseResult)) {
                            //解析是否含有误报库信息
                            RequestResultExpandDTO expandDTO = ResponseUtil.parseByRequestResult(baseResult);
                            dto.setContent(StringUtils.join("result_", JSON.toJSONString(expandDTO)));
                            if (StringUtils.equalsAnyIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name()) && dto.getContent().startsWith("result_")) {
                                apiDefinitionEnvService.setEnvAndPoolName(dto);
                            }
                        }
                    }
                }
                WebSocketUtil.sendMessageSingle(dto);
            }
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        }
    }
}
