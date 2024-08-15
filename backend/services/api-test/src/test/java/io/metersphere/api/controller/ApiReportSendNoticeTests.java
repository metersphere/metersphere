package io.metersphere.api.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.domain.*;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.api.service.ApiReportSendNoticeService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiReportSendNoticeTests extends BaseTest {


    @Resource
    private ApiReportService apiReportService;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiReportSendNoticeService apiReportSendNoticeService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Test
    @Order(0)
    public void sendNoticeTest() throws Exception {
        LocaleContextHolder.setLocale(new Locale("zh", "CN"));
        Environment environment = new Environment();
        environment.setId("api-environment-id");
        environment.setName("api-environment-name");
        environment.setProjectId(DEFAULT_PROJECT_ID);
        environment.setCreateUser("admin");
        environment.setUpdateUser("admin");
        environment.setUpdateTime(System.currentTimeMillis());
        environment.setCreateTime(System.currentTimeMillis());
        environment.setPos(1L);
        environmentMapper.insertSelective(environment);

        ApiDefinition apiDefinition = new ApiDefinition();
        apiDefinition.setId(IDGenerator.nextStr());
        apiDefinition.setProjectId(DEFAULT_PROJECT_ID);
        apiDefinition.setName(StringUtils.join("接口定义", apiDefinition.getId()));
        apiDefinition.setModuleId("case-moduleId");
        apiDefinition.setProtocol(ApiConstants.HTTP_PROTOCOL);
        apiDefinition.setMethod("GET");
        apiDefinition.setStatus(ApiDefinitionStatus.DEBUGGING.name());
        apiDefinition.setNum(NumGenerator.nextNum(DEFAULT_PROJECT_ID, ApplicationNumScope.API_DEFINITION));
        apiDefinition.setPos(0L);
        apiDefinition.setPath(StringUtils.join("api/definition/", apiDefinition.getId()));
        apiDefinition.setLatest(true);
        apiDefinition.setVersionId("1.0");
        apiDefinition.setRefId(apiDefinition.getId());
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setCreateUser("admin");
        apiDefinition.setUpdateUser("admin");
        apiDefinitionMapper.insertSelective(apiDefinition);

        ApiTestCase apiTestCase = new ApiTestCase();
        apiTestCase.setId("send-api-case-id");
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setProjectId(DEFAULT_PROJECT_ID);
        apiTestCase.setName(StringUtils.join("接口用例", apiTestCase.getId()));
        apiTestCase.setPriority("P0");
        apiTestCase.setStatus("Underway");
        apiTestCase.setNum(1111L);
        apiTestCase.setPos(0L);
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setCreateUser("admin");
        apiTestCase.setUpdateUser("admin");
        apiTestCase.setVersionId("1.0");
        apiTestCase.setDeleted(false);
        apiTestCaseMapper.insertSelective(apiTestCase);

        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("send-api-case-report-id" + i);
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setName("api-report-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id");
            apiReport.setEnvironmentId("api-environment-id");
            apiReport.setRunMode("api-run-mode");
            if (i == 0) {
                apiReport.setStatus(ResultStatus.SUCCESS.name());
            } else if (i == 1) {
                apiReport.setStatus(ResultStatus.ERROR.name());
            } else {
                apiReport.setStatus(ResultStatus.FAKE_ERROR.name());
            }
            apiReport.setTriggerMode("api-trigger-mode" + i);
            reports.add(apiReport);
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiTestCaseId("send-api-resource-id" + i);
            record.setApiReportId(apiReport.getId());
            records.add(record);
        }
        apiReportService.insertApiReport(reports, records);
        ApiNoticeDTO noticeDTO = new ApiNoticeDTO();
        noticeDTO.setReportId("send-api-case-report-id0");
        noticeDTO.setReportStatus(ResultStatus.SUCCESS.name());
        noticeDTO.setResourceId("send-api-case-id");
        noticeDTO.setResourceType("API_CASE");
        noticeDTO.setUserId("admin");
        noticeDTO.setProjectId(DEFAULT_PROJECT_ID);
        noticeDTO.getRunModeConfig().setEnvironmentId("api-environment-id");

        apiReportSendNoticeService.sendNotice(noticeDTO);
        noticeDTO.setReportStatus(ResultStatus.ERROR.name());
        noticeDTO.setReportId("send-api-case-report-id1");
        apiReportSendNoticeService.sendNotice(noticeDTO);
        noticeDTO.setReportStatus(ResultStatus.FAKE_ERROR.name());
        noticeDTO.setReportId("send-api-case-report-id2");
        apiReportSendNoticeService.sendNotice(noticeDTO);

        noticeDTO.setResourceType(ApiExecuteResourceType.PLAN_RUN_API_CASE.name());
        apiReportSendNoticeService.sendNotice(noticeDTO);

        noticeDTO.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_CASE.name());
        apiReportSendNoticeService.sendNotice(noticeDTO);


        ApiScenario apiScenario = new ApiScenario();
        apiScenario.setId("send-scenario-id");
        apiScenario.setProjectId(DEFAULT_PROJECT_ID);
        apiScenario.setName("api-scenario-name");
        apiScenario.setCreateUser("admin");
        apiScenario.setUpdateUser("admin");
        apiScenario.setPriority("P0");
        apiScenario.setStatus("Underway");
        apiScenario.setNum(1111L);
        apiScenario.setUpdateTime(System.currentTimeMillis());
        apiScenario.setCreateTime(System.currentTimeMillis());
        apiScenario.setPos(1L);
        apiTestCase.setVersionId("1.0");
        apiScenario.setDeleted(false);
        apiScenario.setVersionId("1.0");
        apiScenario.setRefId("api-ref-id");
        apiScenario.setModuleId("api-module-id");
        apiScenarioMapper.insertSelective(apiScenario);

        List<ApiScenarioReport> scenarioReports = new ArrayList<>();
        List<ApiScenarioRecord> scenarioRecords = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("send-scenario-report-id" + i);
            scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
            scenarioReport.setName("scenario-report-name" + i);
            scenarioReport.setStartTime(System.currentTimeMillis());
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            if (i == 0) {
                scenarioReport.setStatus(ResultStatus.SUCCESS.name());
            } else if (i == 1) {
                scenarioReport.setStatus(ResultStatus.ERROR.name());
            } else {
                scenarioReport.setStatus(ResultStatus.FAKE_ERROR.name());
            }
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("api-pool-id" + i);
            scenarioReport.setEnvironmentId("api-environment-id");
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("api-trigger-mode" + i);
            scenarioReports.add(scenarioReport);
            ApiScenarioRecord record = new ApiScenarioRecord();
            record.setApiScenarioId("send-scenario-id" + i);
            record.setApiScenarioReportId(scenarioReport.getId());
            scenarioRecords.add(record);
        }
        apiScenarioReportService.insertApiScenarioReport(scenarioReports, scenarioRecords);


        noticeDTO = new ApiNoticeDTO();
        noticeDTO.setReportId("send-scenario-report-id0");
        noticeDTO.setReportStatus(ResultStatus.SUCCESS.name());
        noticeDTO.setResourceId("send-api-case-id");
        noticeDTO.setResourceType("API_SCENARIO");
        noticeDTO.setUserId("admin");
        noticeDTO.setProjectId(DEFAULT_PROJECT_ID);
        noticeDTO.getRunModeConfig().setEnvironmentId("api-environment-id");

        apiReportSendNoticeService.sendNotice(noticeDTO);
        noticeDTO.setReportStatus(ResultStatus.ERROR.name());
        noticeDTO.setReportId("send-scenario-report-id1");
        apiReportSendNoticeService.sendNotice(noticeDTO);
        noticeDTO.setReportStatus(ResultStatus.FAKE_ERROR.name());
        noticeDTO.setReportId("send-scenario-report-id2");
        apiReportSendNoticeService.sendNotice(noticeDTO);

        noticeDTO.setResourceType(ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name());
        apiReportSendNoticeService.sendNotice(noticeDTO);

        noticeDTO.setResourceType(ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name());
        apiReportSendNoticeService.sendNotice(noticeDTO);
    }
}
