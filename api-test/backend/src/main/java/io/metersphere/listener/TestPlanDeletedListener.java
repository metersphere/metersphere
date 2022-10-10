package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.plan.TestPlanApiCaseService;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TestPlanDeletedListener {
    public static final String CONSUME_ID = "API_" + KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC;

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String planId = record.value();
        LogUtil.info("api service consume TEST_PLAN_DELETED_TOPIC message, plan id: " + planId);
        testPlanApiCaseService.deleteByPlanId(planId);
        testPlanScenarioCaseService.deleteByPlanId(planId);
    }
}
