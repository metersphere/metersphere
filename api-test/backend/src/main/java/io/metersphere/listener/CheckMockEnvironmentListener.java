package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.environment.service.BaseEnvironmentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CheckMockEnvironmentListener {

    public static final String CONSUME_ID = "check-mock-env";

    @Resource
    private BaseEnvironmentService baseEnvironmentService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.CHECK_MOCK_ENV_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String baseUrl = record.value();
        LogUtil.info("consume check-mock-env message, base url: " + baseUrl);
        if (StringUtils.isNotBlank(baseUrl)) {
            baseEnvironmentService.checkMockEvnInfoByBaseUrl(baseUrl);
        }
    }
}
