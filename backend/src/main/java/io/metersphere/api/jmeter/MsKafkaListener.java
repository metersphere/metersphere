package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.api.service.ApiExecutionQueueService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.config.KafkaConfig;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import javax.annotation.Resource;
import java.util.*;

@Configuration
public class MsKafkaListener {
    public static final String CONSUME_ID = "ms-api-exec-consume";
    @Resource
    private ApiExecutionQueueService apiExecutionQueueService;

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

    @KafkaListener(id = CONSUME_ID, topics = KafkaConfig.TOPICS, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "batchFactory")
    public void consume(List<ConsumerRecord<?, String>> records, Acknowledgment ack) {
        try {
            LoggerUtil.info("进入KAFKA消费，接收到执行结果开始存储：" + records.size());
            // 分三类存储
            Map<String, List<ResultDTO>> assortMap = new LinkedHashMap<>();
            List<ResultDTO> resultDTOS = new LinkedList<>();

            records.forEach(record -> {
                ResultDTO testResult = this.formatResult(record.value());
                if (testResult != null) {
                    if (testResult.getArbitraryData() != null && testResult.getArbitraryData().containsKey("TEST_END") && (Boolean) testResult.getArbitraryData().get("TEST_END")) {
                        resultDTOS.add(testResult);
                    } else {
                        String key = RUN_MODE_MAP.get(testResult.getRunMode());
                        if (assortMap.containsKey(key)) {
                            assortMap.get(key).add(testResult);
                        } else {
                            assortMap.put(key, new LinkedList<ResultDTO>() {{
                                this.add(testResult);
                            }});
                        }
                    }
                }
            });
            if (!assortMap.isEmpty()) {
                testResultService.batchSaveResults(assortMap);
                LoggerUtil.info("KAFKA消费执行内容存储结束");
            }
            // 更新执行结果
            if (CollectionUtils.isNotEmpty(resultDTOS)) {
                resultDTOS.forEach(testResult -> {
                    LoggerUtil.info("报告 【 " + testResult.getReportId() + " 】资源 " + testResult.getTestId() + " 整体执行完成");
                    testResultService.testEnded(testResult);
                    LoggerUtil.info("执行队列处理：" + testResult.getQueueId());
                    apiExecutionQueueService.queueNext(testResult);
                    // 全局并发队列
                    PoolExecBlockingQueueUtil.offer(testResult.getReportId());
                    // 更新测试计划报告
                    if (StringUtils.isNotEmpty(testResult.getTestPlanReportId())) {
                        LoggerUtil.info("Check Processing Test Plan report status：" + testResult.getQueueId() + "，" + testResult.getTestId());
                        apiExecutionQueueService.testPlanReportTestEnded(testResult.getTestPlanReportId());
                    }
                });
            }
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        } finally {
            ack.acknowledge();
        }
    }

    @Resource
    private TestResultService testResultService;

    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;

    private ResultDTO formatResult(String result) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (StringUtils.isNotEmpty(result)) {
                ResultDTO element = mapper.readValue(result, new TypeReference<ResultDTO>() {
                });
                if (StringUtils.isNotEmpty(element.getRunningDebugSampler())) {
                    String evnStr = element.getRunningDebugSampler();
                    apiEnvironmentRunningParamService.parseEvn(evnStr);
                }
                LoggerUtil.info("formatResult 完成：" + element.getReportId());
                return element;
            }
        } catch (Exception e) {
            LoggerUtil.error("formatResult 格式化数据失败：", e);
        }
        return null;
    }
}
