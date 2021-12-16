package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.exec.queue.PoolExecBlockingQueueUtil;
import io.metersphere.api.exec.queue.SerialBlockingQueueUtil;
import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.config.KafkaConfig;
import io.metersphere.dto.ResultDTO;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MsKafkaListener {
    public static final String CONSUME_ID = "ms-api-exec-consume";

    @KafkaListener(id = CONSUME_ID, topics = KafkaConfig.TOPICS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LoggerUtil.info("接收到执行结果开始存储");
        ResultDTO testResult = this.formatResult(record.value());
        if (testResult != null && testResult.getArbitraryData() != null && testResult.getArbitraryData().containsKey("TEST_END") && (Boolean) testResult.getArbitraryData().get("TEST_END")) {
            LoggerUtil.info("报告 【 " + testResult.getReportId() + " 】资源 " + testResult.getTestId() + " 整体执行完成");
            testResultService.testEnded(testResult);
            // 串行队列
            if (CollectionUtils.isEmpty(testResult.getRequestResults())) {
                SerialBlockingQueueUtil.offer(testResult, SerialBlockingQueueUtil.END_SIGN);
            }
            // 全局并发队列
            PoolExecBlockingQueueUtil.offer(testResult.getReportId());
        } else {
            testResultService.saveResults(testResult);
        }
        LoggerUtil.info("执行内容存储结束");
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
            LoggerUtil.error("formatResult 格式化数据失败：" + e.getMessage());
        }
        return null;
    }
}
