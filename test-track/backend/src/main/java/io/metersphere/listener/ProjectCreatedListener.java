package io.metersphere.listener;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.TestCaseNodeService;
import jakarta.annotation.Resource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectCreatedListener {
    public static final String CONSUME_ID = "test_track_project-created";

    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TestCaseNodeService testCaseNodeService;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_CREATED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("track service consume project_create message, project id: " + projectId);
        this.initProjectDefaultNode(projectId);
    }

    private void initProjectDefaultNode(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        // 创建功能用例默认模块
        testCaseNodeService.createDefaultNode(projectId);
    }
}
