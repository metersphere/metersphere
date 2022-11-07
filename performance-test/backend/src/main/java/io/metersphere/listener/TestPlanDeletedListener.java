package io.metersphere.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plan.service.TestPlanLoadCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class TestPlanDeletedListener {
    public static final String CONSUME_ID = "PERFORMANCE_" + KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC;

    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private ObjectMapper objectMapper;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        try {
            List<String> planIds = objectMapper.readValue(record.value(), new TypeReference<>() {});
            if (CollectionUtils.isEmpty(planIds)) {
                return;
            }
            LogUtil.info("performance service consume TEST_PLAN_DELETED_TOPIC message, plans: " + planIds);
            testPlanLoadCaseService.deleteByPlanIds(planIds);
        } catch (Exception e) {
            LogUtil.error("performance service consume TEST_PLAN_DELETED_TOPIC message error, plans: " + record.value(), e);
        }
    }
}
