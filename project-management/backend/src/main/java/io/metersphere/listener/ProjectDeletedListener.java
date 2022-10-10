package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.ProjectApplicationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProjectDeletedListener {

    public static final String CONSUME_ID = "project_management-deleted";

    @Resource
    private ProjectApplicationService projectApplicationService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("project management service consume project_delete message, project id: " + projectId);
        projectApplicationService.deleteRelateProjectConfig(projectId);
    }
}
