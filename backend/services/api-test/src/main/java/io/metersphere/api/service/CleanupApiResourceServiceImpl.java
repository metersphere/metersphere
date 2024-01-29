package io.metersphere.api.service;

import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.schedule.SwaggerUrlImportJob;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.domain.ShareInfoExample;
import io.metersphere.sdk.mapper.ShareInfoMapper;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.domain.ScheduleExample;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.schedule.ScheduleService;
import io.metersphere.system.service.CleanupProjectResourceService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = Exception.class)
public class CleanupApiResourceServiceImpl implements CleanupProjectResourceService {

    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseFollowerMapper apiTestCaseFollowerMapper;
    @Resource
    private ApiDefinitionFollowerMapper apiDefinitionFollowerMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiTestCaseBlobMapper apiTestCaseBlobMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiFileResourceService apiFileResourceService;
    @Resource
    private ExtApiDefinitionMockMapper extApiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMockConfigMapper apiDefinitionMockConfigMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ExtApiReportMapper extApiReportMapper;
    @Resource
    private ApiReportStepMapper apiReportStepMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;
    @Resource
    private ApiReportLogMapper apiReportLogMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportStepMapper apiScenarioReportStepMapper;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioReportLogMapper apiScenarioReportLogMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ShareInfoMapper shareInfoMapper;
    @Resource
    private ApiDefinitionSwaggerMapper apiDefinitionSwaggerMapper;


    @Async
    @Override
    public void deleteResources(String projectId) {
        LogUtils.info("删除当前项目[" + projectId + "]相关接口测试资源");
        //删除模块
        delApiModule(projectId);
        delScenarioModule(projectId);
        //删除接口
        delApi(projectId);
        //删除报告 删除执行记录
        deleteReport(projectId);
        //删除分享报告的数据
        deleteShareUrl(projectId);
        //删除定时任务
        deleteSchedule(projectId);

    }

    private void deleteShareUrl(String projectId) {
        ShareInfoExample example = new ShareInfoExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        shareInfoMapper.deleteByExample(example);
    }

    private void delScenarioModule(String projectId) {
        ApiScenarioModuleExample apiScenarioModuleExample = new ApiScenarioModuleExample();
        apiScenarioModuleExample.createCriteria().andProjectIdEqualTo(projectId);
        apiScenarioModuleMapper.deleteByExample(apiScenarioModuleExample);
    }

    private void delApiModule(String projectId) {
        ApiDefinitionModuleExample apiModuleExample = new ApiDefinitionModuleExample();
        apiModuleExample.createCriteria().andProjectIdEqualTo(projectId);
        apiDefinitionModuleMapper.deleteByExample(apiModuleExample);
    }

    /**
     * 删除接口
     * 有可能及联数据没有删干净  需要补充
     */
    private void delApi(String projectId) {
        List<String> apiIds = extApiDefinitionMapper.selectByProjectId(projectId);
        if (CollectionUtils.isNotEmpty(apiIds)) {
            SubListUtils.dealForSubList(apiIds, 500, subList -> {
                List<String> caseIds = extApiTestCaseMapper.getIdsByApiIds(subList);
                if (CollectionUtils.isNotEmpty(caseIds)) {
                    deleteCase(caseIds, projectId);
                }
                List<String> mockIds = extApiDefinitionMockMapper.getIdsByApiIds(subList);
                if (CollectionUtils.isNotEmpty(mockIds)) {
                    deleteMock(mockIds, projectId);
                }
                deleteApi(subList, projectId);
            });
        }
    }

    private void deleteApiFollows(List<String> ids) {
        ApiDefinitionFollowerExample apiExample = new ApiDefinitionFollowerExample();
        apiExample.createCriteria().andApiDefinitionIdIn(ids);
        apiDefinitionFollowerMapper.deleteByExample(apiExample);
    }

    private void deleteCaseFollows(List<String> ids) {
        ApiTestCaseFollowerExample example = new ApiTestCaseFollowerExample();
        example.createCriteria().andCaseIdIn(ids);
        apiTestCaseFollowerMapper.deleteByExample(example);
    }

    private void deleteCase(List<String> ids, String projectId) {
        deleteCaseFollows(ids);
        ids.forEach(id -> {
            String apiCaseDir = DefaultRepositoryDir.getApiCaseDir(projectId, id);
            apiFileResourceService.deleteByResourceId(apiCaseDir, id, projectId, OperationLogConstants.SYSTEM, OperationLogModule.API_DEFINITION_CASE);
        });
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
        ApiTestCaseBlobExample blobExample = new ApiTestCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        apiTestCaseBlobMapper.deleteByExample(blobExample);
    }

    private void deleteMock(List<String> ids, String projectId) {
        ids.forEach(id -> {
            String apiCaseDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, id);
            apiFileResourceService.deleteByResourceId(apiCaseDir, id, projectId, OperationLogConstants.SYSTEM, OperationLogModule.API_DEFINITION_CASE);
        });
        ApiDefinitionMockConfigExample configExample = new ApiDefinitionMockConfigExample();
        configExample.createCriteria().andIdIn(ids);
        apiDefinitionMockConfigMapper.deleteByExample(configExample);
        ApiDefinitionMockExample mockExample = new ApiDefinitionMockExample();
        mockExample.createCriteria().andIdIn(ids);
        apiDefinitionMockMapper.deleteByExample(mockExample);
    }

    private void deleteApi(List<String> ids, String projectId) {
        deleteApiFollows(ids);
        ids.forEach(id -> {
            String apiDefinitionDir = DefaultRepositoryDir.getApiDefinitionDir(projectId, id);
            apiFileResourceService.deleteByResourceId(apiDefinitionDir, id, projectId, OperationLogConstants.SYSTEM, OperationLogModule.API_DEFINITION);
        });
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        apiDefinitionMapper.deleteByExample(example);
        ApiTestCaseBlobExample blobExample = new ApiTestCaseBlobExample();
        blobExample.createCriteria().andIdIn(ids);
        apiTestCaseBlobMapper.deleteByExample(blobExample);
    }

    private void deleteReport(String projectId) {
        ApiReportExample reportExample = new ApiReportExample();
        reportExample.createCriteria().andProjectIdEqualTo(projectId);
        long apiCount = apiReportMapper.countByExample(reportExample);
        while (apiCount > 0) {
            List<String> ids = extApiReportMapper.selectApiReportByProjectId(projectId);
            ApiReportExample example = new ApiReportExample();
            example.createCriteria().andIdIn(ids);
            apiReportMapper.deleteByExample(example);
            deleteApiReport(ids);
            apiCount = apiReportMapper.countByExample(reportExample);
        }

        ApiScenarioReportExample scenarioReportExample = new ApiScenarioReportExample();
        scenarioReportExample.createCriteria().andProjectIdEqualTo(projectId);
        long scenarioCount = apiScenarioReportMapper.countByExample(scenarioReportExample);
        while (scenarioCount > 0) {
            List<String> ids = extApiScenarioReportMapper.selectApiScenarioReportByProjectId(projectId);
            ApiScenarioReportExample example = new ApiScenarioReportExample();
            example.createCriteria().andIdIn(ids);
            apiScenarioReportMapper.deleteByExample(example);
            deleteScenarioReport(ids);
            scenarioCount = apiReportMapper.countByExample(reportExample);
        }

    }

    private void deleteApiReport(List<String> ids) {
        ApiReportStepExample stepExample = new ApiReportStepExample();
        stepExample.createCriteria().andReportIdIn(ids);
        apiReportStepMapper.deleteByExample(stepExample);
        ApiReportDetailExample detailExample = new ApiReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiReportDetailMapper.deleteByExample(detailExample);
        ApiReportLogExample logExample = new ApiReportLogExample();
        logExample.createCriteria().andReportIdIn(ids);
        apiReportLogMapper.deleteByExample(logExample);
    }

    private void deleteScenarioReport(List<String> ids) {
        ApiScenarioReportStepExample stepExample = new ApiScenarioReportStepExample();
        stepExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportStepMapper.deleteByExample(stepExample);
        ApiScenarioReportDetailExample detailExample = new ApiScenarioReportDetailExample();
        detailExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportDetailMapper.deleteByExample(detailExample);
        ApiScenarioReportLogExample logExample = new ApiScenarioReportLogExample();
        logExample.createCriteria().andReportIdIn(ids);
        apiScenarioReportLogMapper.deleteByExample(logExample);
    }

    private void deleteSchedule(String projectId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andProjectIdEqualTo(projectId).andJobEqualTo(SwaggerUrlImportJob.class.getName());
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        if (CollectionUtils.isNotEmpty(schedules)) {
            scheduleService.deleteByResourceIds(schedules.stream().map(Schedule::getResourceId).collect(Collectors.toList()), SwaggerUrlImportJob.class.getName());
        }
        ApiDefinitionSwaggerExample swaggerExample = new ApiDefinitionSwaggerExample();
        swaggerExample.createCriteria().andProjectIdEqualTo(projectId);
        apiDefinitionSwaggerMapper.deleteByExample(swaggerExample);
        //TODO 删除场景的定时任务

    }

}
