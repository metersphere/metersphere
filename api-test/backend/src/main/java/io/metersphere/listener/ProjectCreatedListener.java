package io.metersphere.listener;

import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ModuleNode;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ext.BaseModuleNodeMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class ProjectCreatedListener {

    public static final String CONSUME_ID = "api-project-created";

    @Resource
    private BaseModuleNodeMapper baseModuleNodeMapper;
    @Resource
    private ApiModuleMapper apiModuleMapper;


    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_CREATED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("api service consume project_create message, project id: " + projectId);
        this.initProjectDefaultNode(projectId);
    }

    private void initProjectDefaultNode(String projectId) {
        ModuleNode record = new ModuleNode();
        record.setId(UUID.randomUUID().toString());
        record.setCreateUser(SessionUtils.getUserId());
        record.setPos(1.0);
        record.setLevel(1);
        record.setCreateTime(System.currentTimeMillis());
        record.setUpdateTime(System.currentTimeMillis());
        record.setProjectId(projectId);
        record.setName(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getNodeName());
        baseModuleNodeMapper.insert(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getTableName(), record);
        ApiModule apiRecord = new ApiModule();
        BeanUtils.copyBean(apiRecord, record);
        apiRecord.setName(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName());
        String[] protocols = {"HTTP", "DUBBO", "SQL", "TCP"};
        for (String protocol : protocols) {
            apiRecord.setProtocol(protocol);
            apiRecord.setId(UUID.randomUUID().toString());
            apiModuleMapper.insert(apiRecord);
        }
    }
}
