package io.metersphere.listener;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiModuleMapper;
import io.metersphere.base.mapper.ApiScenarioModuleMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.BaseModuleNodeMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.ProjectModuleDefaultNodeEnum;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
public class ProjectCreatedListener {

    public static final String CONSUME_ID = "api-project-created";

    @Resource
    private BaseModuleNodeMapper baseModuleNodeMapper;
    @Resource
    private ApiModuleMapper apiModuleMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ProjectMapper projectMapper;


    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_CREATED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("api service consume project_create message, project id: " + projectId);
        this.initProjectDefaultNode(projectId);
    }

    private void initProjectDefaultNode(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        ModuleNode record = new ModuleNode();
        record.setId(UUID.randomUUID().toString());
        record.setCreateUser(project.getCreateUser());
        record.setPos(1.0);
        record.setLevel(1);
        record.setCreateTime(System.currentTimeMillis());
        record.setUpdateTime(System.currentTimeMillis());
        record.setProjectId(projectId);
        record.setName(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getNodeName());
        ApiScenarioModuleExample scenarioModuleExample = new ApiScenarioModuleExample();
        scenarioModuleExample.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getNodeName()).andParentIdIsNull();
        List<ApiScenarioModule> scenarioModules = apiScenarioModuleMapper.selectByExample(scenarioModuleExample);
        if (CollectionUtils.isEmpty(scenarioModules)) {
            baseModuleNodeMapper.insert(ProjectModuleDefaultNodeEnum.API_SCENARIO_DEFAULT_NODE.getTableName(), record);
        }
        ApiModule apiRecord = new ApiModule();
        BeanUtils.copyBean(apiRecord, record);
        apiRecord.setName(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName());
        String[] protocols = {"HTTP", "DUBBO", "SQL", "TCP"};
        for (String protocol : protocols) {
            apiRecord.setProtocol(protocol);
            apiRecord.setId(UUID.randomUUID().toString());
            ApiModuleExample apiExample = new ApiModuleExample();
            apiExample.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(protocol).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName()).andParentIdIsNull();
            List<ApiModule> apiList = apiModuleMapper.selectByExample(apiExample);
            if (CollectionUtils.isEmpty(apiList)) {
                apiModuleMapper.insert(apiRecord);
            }
        }
    }
}
