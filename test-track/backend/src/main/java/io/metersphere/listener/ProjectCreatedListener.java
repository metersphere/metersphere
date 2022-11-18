package io.metersphere.listener;

import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.TestCaseNodeExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtModuleNodeMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.utils.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
public class ProjectCreatedListener {
    public static final String CONSUME_ID = "test_track_project-created";

    @Resource
    private ExtModuleNodeMapper extModuleNodeMapper;
    @Resource
    private ProjectMapper projectMapper;

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

        // 防止重复创建功能用例默认节点
        TestCaseNodeExample example = new TestCaseNodeExample();
        example.createCriteria()
                .andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.TEST_CASE_DEFAULT_NODE.getNodeName());
        List<ModuleNode> moduleNodes = extModuleNodeMapper.selectByExample(ProjectModuleDefaultNodeEnum.TEST_CASE_DEFAULT_NODE.getTableName(), example);
        if (moduleNodes.size() == 0) {
            ModuleNode record = new ModuleNode();
            record.setId(UUID.randomUUID().toString());
            record.setCreateUser(project.getCreateUser());
            record.setPos(1.0);
            record.setLevel(1);
            record.setCreateTime(System.currentTimeMillis());
            record.setUpdateTime(System.currentTimeMillis());
            record.setProjectId(projectId);
            record.setName(ProjectModuleDefaultNodeEnum.TEST_CASE_DEFAULT_NODE.getNodeName());
            extModuleNodeMapper.insert(ProjectModuleDefaultNodeEnum.TEST_CASE_DEFAULT_NODE.getTableName(), record);
        }
    }
}
