package io.metersphere.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.service.ApiEnvironmentRunningParamService;
import io.metersphere.api.service.TestResultService;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MsKafkaListener {
    public static final String TOPICS = "ms-api-exec-topic";
    public static final String CONSUME_ID = "ms-api-exec-consume";

    @KafkaListener(id = CONSUME_ID, topics = TOPICS, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, String> record) {
        LogUtil.info("接收到执行结果开始存储");
        this.save(record.value());
        LogUtil.info("执行内容存储结束");
    }

    @Resource
    private TestResultService testResultService;

    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;

    private TestResult formatResult(String result) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
            if (StringUtils.isNotEmpty(result)) {
                TestResult element = mapper.readValue(result, new TypeReference<TestResult>() {
                });
                if (StringUtils.isNotEmpty(element.getRunningDebugSampler())) {
                    String evnStr = element.getRunningDebugSampler();
                    apiEnvironmentRunningParamService.parseEvn(evnStr);
                }
                return element;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage());
        }
        return null;
    }

    private void save(String execResult) {
        TestResult testResult = this.formatResult(execResult);
        testResultService.saveResult(testResult, testResult.getRunMode(), null, testResult.getTestId());
    }
}
