package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plan.service.TestPlanLoadCaseService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TestPlanDeletedListener {
    public static final String CONSUME_ID = "PERFORMANCE_" + KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC;

    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String planId = record.value();
        LogUtil.info("performance service consume TEST_PLAN_DELETED_TOPIC message, plan id: " + planId);
        testPlanLoadCaseService.deleteByPlanId(planId);
    }
}
