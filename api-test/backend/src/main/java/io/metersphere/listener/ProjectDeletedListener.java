package io.metersphere.listener;

import io.metersphere.service.ext.ExtApiScheduleService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportStructureMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Component
@Transactional(rollbackFor = Exception.class)
public class ProjectDeletedListener {

    @Resource
    private ExtApiScheduleService scheduleService;
    @Resource
    private ApiModuleMapper apiModuleMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ExtApiScenarioReportResultMapper extApiScenarioReportResultMapper;
    @Resource
    private ExtApiScenarioReportStructureMapper extApiScenarioReportStructureMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;

    public static final String CONSUME_ID = "project-deleted";

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("api service consume project_delete message, project id: " + projectId);
        deleteAPIResourceByProjectId(projectId);
    }

    private void deleteAPIResourceByProjectId(String projectId) {
        //删除报告
        delReport(projectId);
        //删除定时任务
        scheduleService.deleteByProjectId(projectId);
        //删除模块
        delApiModuleByProjectId(projectId);
        delScenarioModuleByProjectId(projectId);
        //删除接口
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andProjectIdEqualTo(projectId);
        apiDefinitionMapper.deleteByExample(apiDefinitionExample);
        //删除case
        ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
        apiTestCaseExample.createCriteria().andProjectIdEqualTo(projectId);
        apiTestCaseMapper.deleteByExample(apiTestCaseExample);
        //删除场景
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andProjectIdEqualTo(projectId);
        apiScenarioMapper.deleteByExample(apiScenarioExample);
    }

    private void delReport(String projectId) {
        extApiScenarioReportResultMapper.deleteByProjectId(projectId);
        extApiScenarioReportStructureMapper.deleteByProjectId(projectId);
        ApiScenarioReportExample apiScenarioReportExample = new ApiScenarioReportExample();
        apiScenarioReportExample.createCriteria().andProjectIdEqualTo(projectId);
        apiScenarioReportMapper.deleteByExample(apiScenarioReportExample);
        ApiScenarioReportDetailExample apiScenarioReportDetailExample = new ApiScenarioReportDetailExample();
        apiScenarioReportDetailExample.createCriteria().andProjectIdEqualTo(projectId);
        apiScenarioReportDetailMapper.deleteByExample(apiScenarioReportDetailExample);
    }

    private void delScenarioModuleByProjectId(String projectId) {
        ApiScenarioModuleExample apiScenarioModuleExample = new ApiScenarioModuleExample();
        apiScenarioModuleExample.createCriteria().andProjectIdEqualTo(projectId);
        apiScenarioModuleMapper.deleteByExample(apiScenarioModuleExample);
    }

    private void delApiModuleByProjectId(String projectId) {
        ApiModuleExample apiModuleExample = new ApiModuleExample();
        apiModuleExample.createCriteria().andProjectIdEqualTo(projectId);
        apiModuleMapper.deleteByExample(apiModuleExample);
    }

}
