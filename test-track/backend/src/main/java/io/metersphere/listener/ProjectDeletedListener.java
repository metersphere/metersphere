package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plan.service.TestPlanProjectService;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.service.TestCaseService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ProjectDeletedListener {
    public static final String CONSUME_ID = "test_track_project-deleted";

    @Resource
    private TestPlanProjectService testPlanProjectService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private TestCaseService testCaseService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("track service consume project_delete message, project id: " + projectId);
        deleteTrackResourceByProjectId(projectId);
    }

    private void deleteTrackResourceByProjectId(String projectId) {
        List<String> testPlanIds = testPlanProjectService.getPlanIdByProjectId(projectId);
        if (!CollectionUtils.isEmpty(testPlanIds)) {
            testPlanIds.forEach(testPlanId -> {
                testPlanService.deleteTestPlan(testPlanId);
            });
        }
        testCaseService.deleteTestCaseByProjectId(projectId);
    }
}
