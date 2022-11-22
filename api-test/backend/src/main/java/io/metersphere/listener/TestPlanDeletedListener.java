package io.metersphere.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.plan.TestPlanApiCaseService;
import io.metersphere.service.plan.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class TestPlanDeletedListener {
    public static final String CONSUME_ID = "API_" + KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC;

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    private ObjectMapper objectMapper;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.TEST_PLAN_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        try {
            List<String> planIds = objectMapper.readValue(record.value(), new TypeReference<>() {
            });
            if (CollectionUtils.isEmpty(planIds)) {
                return;
            }
            LogUtil.info("api service consume TEST_PLAN_DELETED_TOPIC message, planIds: ", planIds);
            testPlanApiCaseService.deleteByPlanIds(planIds);
            testPlanScenarioCaseService.deleteByPlanIds(planIds);
        } catch (Exception e) {
            LogUtil.error("api service consume TEST_PLAN_DELETED_TOPIC message error, planIds: " + record.value(), e);
        }

    }
}
