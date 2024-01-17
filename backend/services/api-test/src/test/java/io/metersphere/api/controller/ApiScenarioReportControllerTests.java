package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiScenarioReportExample;
import io.metersphere.api.domain.ApiScenarioReportStep;
import io.metersphere.api.domain.ApiScenarioReportStepExample;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ApiScenarioReportStepMapper;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.system.base.BaseTest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiScenarioReportControllerTests extends BaseTest {

    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportStepMapper apiScenarioReportStepMapper;

    @Test
    @Order(1)
    public void testInsert() {
        List<ApiScenarioReport> reports = new ArrayList<>();
        for (int i = 0; i < 2515; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("api-report-id" + i);
            scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
            scenarioReport.setName("api-report-name" + i);
            scenarioReport.setStartTime(System.currentTimeMillis());
            scenarioReport.setScenarioId("api-scenario-id" + i);
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("api-pool-id" + i);
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("api-trigger-mode" + i);
            scenarioReport.setVersionId("api-version-id" + i);
            reports.add(scenarioReport);
        }
        apiScenarioReportService.insertApiScenarioReport(reports);
        List<ApiScenarioReport> reports1 = apiScenarioReportMapper.selectByExample(new ApiScenarioReportExample());
        Assertions.assertEquals(reports1.size(), 2515);

        List<ApiScenarioReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 1515; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("api-report-step-id" + i);
            apiScenarioReportStep.setReportId("api-report-id" + i);
            apiScenarioReportStep.setSort(0L);
            apiScenarioReportStep.setStepType("case");
            steps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(steps);
        List<ApiScenarioReportStep> steps1 = apiScenarioReportStepMapper.selectByExample(new ApiScenarioReportStepExample());
        Assertions.assertEquals(steps1.size(), 1515);
    }

}
