package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.scenario.CsvVariable;
import io.metersphere.api.job.ApiScenarioScheduleJob;
import io.metersphere.api.mapper.*;
import io.metersphere.api.service.CleanupApiReportServiceImpl;
import io.metersphere.api.service.CleanupApiResourceServiceImpl;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.schedule.SwaggerUrlImportJob;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.ScheduleType;
import io.metersphere.system.domain.Schedule;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import io.metersphere.system.mapper.ScheduleMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupApiTests {
    private final ProjectServiceInvoker serviceInvoker;
    @Resource
    private CleanupApiResourceServiceImpl cleanupApiResourceServiceImpl;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ApiScenarioModuleMapper apiScenarioModuleMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private CleanupApiReportServiceImpl cleanupApiReportServiceImpl;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiFileResourceMapper apiFileResourceMapper;
    @Resource
    private ApiScenarioCsvMapper apiScenarioCsvMapper;

    @Autowired
    public CleanupApiTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    public void initData() throws Exception {
        //创建接口模块
        ApiDefinitionModule apiDefinitionModule = new ApiDefinitionModule();
        apiDefinitionModule.setId("test-module");
        apiDefinitionModule.setProjectId("test");
        apiDefinitionModule.setName("test");
        apiDefinitionModule.setPos(1L);
        apiDefinitionModule.setCreateUser("admin");
        apiDefinitionModule.setUpdateUser("admin");
        apiDefinitionModule.setCreateTime(System.currentTimeMillis());
        apiDefinitionModule.setUpdateTime(System.currentTimeMillis());
        apiDefinitionModuleMapper.insertSelective(apiDefinitionModule);
        //创建场景模块
        ApiScenarioModule apiScenarioModule = new ApiScenarioModule();
        apiScenarioModule.setId("test-scenario-module");
        apiScenarioModule.setProjectId("test");
        apiScenarioModule.setName("test");
        apiScenarioModule.setPos(1L);
        apiScenarioModule.setCreateUser("admin");
        apiScenarioModule.setUpdateUser("admin");
        apiScenarioModule.setCreateTime(System.currentTimeMillis());
        apiScenarioModule.setUpdateTime(System.currentTimeMillis());
        apiScenarioModuleMapper.insertSelective(apiScenarioModule);
        //创建接口
        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId("test");
        apiDefinition.setProjectId("test");
        apiDefinition.setModuleId("test-module");
        apiDefinition.setName("test");
        apiDefinition.setPath("test");
        apiDefinition.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinition.setMethod("test");
        apiDefinition.setCreateUser("admin");
        apiDefinition.setUpdateUser("admin");
        apiDefinition.setNum(1L);
        apiDefinition.setVersionId("test");
        apiDefinition.setRefId("test");
        apiDefinition.setPos(1L);
        apiDefinition.setLatest(true);
        apiDefinition.setStatus("api-status");
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.insertSelective(apiDefinition);
        //创建用例
        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("test");
        apiTestCase.setProjectId("test");
        apiTestCase.setApiDefinitionId("test");
        apiTestCase.setCreateUser("admin");
        apiTestCase.setUpdateUser("admin");
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setPos(1L);
        apiTestCase.setNum(1L);
        apiTestCase.setStatus("test");
        apiTestCase.setVersionId("test");
        apiTestCase.setPriority("test");
        apiTestCase.setName("test");
        apiTestCaseMapper.insertSelective(apiTestCase);
        //创建mock
        ApiDefinitionMock apiDefinitionMock = new ApiDefinitionMock();
        apiDefinitionMock.setId("test");
        apiDefinitionMock.setApiDefinitionId("test");
        apiDefinitionMock.setProjectId("test");
        apiDefinitionMock.setCreateUser("admin");
        apiDefinitionMock.setCreateTime(System.currentTimeMillis());
        apiDefinitionMock.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMock.setName("test");
        apiDefinitionMock.setExpectNum("test");
        apiDefinitionMockMapper.insertSelective(apiDefinitionMock);
        //创建场景数据
        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId("system-scenario-id");
        apiScenario.setProjectId("test");
        apiScenario.setName("场景");
        apiScenario.setNum(1L);
        apiScenario.setCreateTime(System.currentTimeMillis());
        apiScenario.setUpdateTime(System.currentTimeMillis());
        apiScenario.setCreateUser("admin");
        apiScenario.setUpdateUser("admin");
        apiScenario.setStatus("未规划");
        apiScenario.setDeleted(false);
        apiScenario.setPriority("P0");
        apiScenario.setStepTotal(0);
        apiScenario.setPos(64L);
        apiScenario.setModuleId("test-default");
        apiScenario.setVersionId("1.0");
        apiScenario.setRequestPassRate(String.valueOf(0));
        apiScenario.setRefId(apiScenario.getId());
        apiScenario.setLatest(true);
        apiScenario.setLastReportStatus("未执行");
        apiScenarioMapper.insertSelective(apiScenario);
        ApiFileResource apiFileResource = new ApiFileResource();
        apiFileResource.setResourceId(apiScenario.getId());
        apiFileResource.setFileId(IDGenerator.nextStr());
        apiFileResource.setFileName("test");
        apiFileResource.setResourceType("API_SCENARIO");
        apiFileResource.setCreateTime(System.currentTimeMillis());
        apiFileResource.setProjectId(apiScenario.getProjectId());
        apiFileResourceMapper.insertSelective(apiFileResource);

        ApiScenarioCsv apiScenarioCsv = new ApiScenarioCsv();
        apiScenarioCsv.setId(IDGenerator.nextStr());
        apiScenarioCsv.setScenarioId(apiScenario.getId());
        apiScenarioCsv.setFileId("fileMetadataId");
        apiScenarioCsv.setName("csv变量");
        apiScenarioCsv.setScope(CsvVariable.CsvVariableScope.SCENARIO.name());
        apiScenarioCsv.setProjectId("test");
        apiScenarioCsvMapper.insertSelective(apiScenarioCsv);

    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        initData();
        initReportData("test");
        initScheduleData();
        cleanupApiResourceServiceImpl.deleteResources("test");
        serviceInvoker.invokeServices("test");
    }

    private void initScheduleData() {
        Schedule schedule = new Schedule();
        schedule.setName("test");
        schedule.setResourceId("test");
        schedule.setEnable(true);
        schedule.setValue("test");
        schedule.setKey("test");
        schedule.setCreateUser("admin");
        schedule.setProjectId("test");
        schedule.setConfig("config");
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        schedule.setNum(1123L);
        scheduleMapper.insertSelective(schedule);
        schedule = new Schedule();
        schedule.setName("test-111");
        schedule.setResourceId("test-111");
        schedule.setEnable(true);
        schedule.setValue("test");
        schedule.setKey("test");
        schedule.setCreateUser("admin");
        schedule.setProjectId("test");
        schedule.setConfig("config");
        schedule.setJob(ApiScenarioScheduleJob.class.getName());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        schedule.setNum(123456L);
        scheduleMapper.insertSelective(schedule);
    }

    @Test
    @Order(2)
    public void testCleanupReport() throws Exception {
        initReportData("test-clean-project");
        Map<String, String> map = new HashMap<>();
        map.put(ProjectApplicationType.API.API_CLEAN_REPORT.name(), "1D");
        cleanupApiReportServiceImpl.cleanReport(map, "test-clean-project");
    }

    private void initReportData(String projectId) {
        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("clean-report-id" + projectId + i);
            apiReport.setProjectId(projectId);
            apiReport.setName("clean-report-name" + i);
            apiReport.setStartTime(1703174400000L);
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id" + i);
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            if (i % 50 == 0) {
                apiReport.setStatus(ResultStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                apiReport.setStatus(ResultStatus.ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            reports.add(apiReport);
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiTestCaseId("clean-resource-id" + i);
            record.setApiReportId(apiReport.getId());
            records.add(record);
        }
        apiReportService.insertApiReport(reports, records);
        List<ApiReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiReportStep apiReportStep = new ApiReportStep();
            apiReportStep.setStepId("clean-api-report-step-id" + projectId + i);
            apiReportStep.setReportId("clean-report-id" + projectId + i);
            apiReportStep.setSort(0L);
            apiReportStep.setStepType(ApiExecuteResourceType.API_CASE.name());
            steps.add(apiReportStep);
        }
        apiReportService.insertApiReportStep(steps);

        List<ApiScenarioReport> scenarioReports = new ArrayList<>();
        List<ApiScenarioRecord> scenarioRecords = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("clean-scenario-report-id" + projectId + i);
            scenarioReport.setProjectId(projectId);
            scenarioReport.setName("clean--scenario-report-name" + i);
            scenarioReport.setStartTime(1703174400000L);
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            if (i % 50 == 0) {
                scenarioReport.setStatus(ResultStatus.SUCCESS.name());
            } else if (i % 39 == 0) {
                scenarioReport.setStatus(ResultStatus.ERROR.name());
            }
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("api-pool-id" + i);
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("api-trigger-mode" + i);
            scenarioReports.add(scenarioReport);
            ApiScenarioRecord record = new ApiScenarioRecord();
            record.setApiScenarioId("clean-scenario-id" + i);
            record.setApiScenarioReportId(scenarioReport.getId());
            scenarioRecords.add(record);
        }
        apiScenarioReportService.insertApiScenarioReport(scenarioReports, scenarioRecords);

        List<ApiScenarioReportStep> scenarioReportSteps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("clean-step-id" + projectId + i);
            apiScenarioReportStep.setReportId("clean-scenario-report-id" + projectId + i);
            apiScenarioReportStep.setSort(0L);
            apiScenarioReportStep.setStepType("case");
            scenarioReportSteps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(scenarioReportSteps);
    }

}
