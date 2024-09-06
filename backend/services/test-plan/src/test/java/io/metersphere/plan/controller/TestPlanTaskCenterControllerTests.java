package io.metersphere.plan.controller;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiTestCaseRecord;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.plan.domain.TestPlanReportApiCase;
import io.metersphere.plan.domain.TestPlanReportApiScenario;
import io.metersphere.plan.mapper.TestPlanReportApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanReportApiScenarioMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanTaskCenterControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/plan/";
    private final static String REAL_TIME_PROJECT_PAGE = BASE_PATH + "project/real-time/page";
    private final static String REAL_TIME_ORG_PAGE = BASE_PATH + "org/real-time/page";
    private final static String REAL_TIME_SYSTEM_PAGE = BASE_PATH + "system/real-time/page";
    private final static String REAL_TIME_PROJECT_STOP = BASE_PATH + "project/stop";
    private final static String REAL_TIME_ORG_STOP = BASE_PATH + "org/stop";
    private final static String REAL_TIME_SYSTEM_STOP = BASE_PATH + "system/stop";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private TestPlanReportApiCaseMapper testPlanReportApiCaseMapper;
    @Resource
    private TestPlanReportApiScenarioMapper testPlanReportApiScenarioMapper;
    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;


    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_task_plan.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {

        doTaskCenterPage("KEYWORD", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPage("FILTER", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_ORG_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPage("FILTER", REAL_TIME_ORG_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPage("FILTER", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
    }

    private void doTaskCenterPage(String search, String url, String moduleType) throws Exception {
        TaskCenterPageRequest request = new TaskCenterPageRequest();
        request.setModuleType(moduleType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("startTime", "asc"));
        // "KEYWORD", "FILTER"
        switch (search) {
            case "KEYWORD" -> configureKeywordSearch(request);
            case "FILTER" -> configureFilterSearch(request);
            default -> {
            }
        }
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();

        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        LogUtils.info(resultHolder);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        Pager<?> pageData = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);
        // 返回值不为空
        Assertions.assertNotNull(pageData);
        // 返回值的页码和当前页码相同
        Assertions.assertEquals(pageData.getCurrent(), request.getCurrent());
        // 返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(JSON.parseArray(JSON.toJSONString(pageData.getList())).size() <= request.getPageSize());

    }

    private void doTaskCenterPageError(String url, String moduleType) throws Exception {
        TaskCenterPageRequest request = new TaskCenterPageRequest();
        request.setModuleType(moduleType);
        request.setCurrent(1);
        request.setPageSize(10);
        request.setSort(Map.of("startTime", "asc"));
        configureKeywordSearch(request);

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, "DEFAULT_PROJECT_ID")
                        .header(SessionConstants.CURRENT_ORGANIZATION, "DEFAULT_ORGANIZATION_ID")
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(ERROR_REQUEST_MATCHER);

    }

    private void configureKeywordSearch(TaskCenterPageRequest request) {
        request.setKeyword("18");
        request.setSort(Map.of("triggerMode", "asc"));
    }

    private void configureFilterSearch(TaskCenterPageRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        request.setSort(Map.of());
        filters.put("execStatus", List.of("RUNNING"));
        request.setFilter(filters);
    }

    @Test
    @Order(10)
    public void getPageError() throws Exception {
        doTaskCenterPageError(REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
        doTaskCenterPageError(REAL_TIME_ORG_PAGE, TaskCenterResourceType.TEST_PLAN.toString());
    }

    @Test
    @Order(11)
    public void stop() throws Exception {
        testInsertData();
        doStop("KEYWORD", REAL_TIME_PROJECT_STOP, false);
        doStop("FILTER", REAL_TIME_PROJECT_STOP, true);
        doStop("KEYWORD", REAL_TIME_ORG_STOP, true);
        doStop("FILTER", REAL_TIME_ORG_STOP, false);
        doStop("KEYWORD", REAL_TIME_SYSTEM_STOP, true);
        doStop("FILTER", REAL_TIME_SYSTEM_STOP, false);

    }

    @Test
    @Order(12)
    public void stopById() throws Exception {
        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/plan/project/stop/test-plan-report-id-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/plan/org/stop/test-plan-report-id-3")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/plan/system/stop/test-plan-report-id-4")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private void configureKeywordBatch(TaskCenterBatchRequest request) {
        TableBatchProcessDTO batchProcessDTO = new TableBatchProcessDTO();
        BaseCondition condition = new BaseCondition();
        condition.setKeyword("task-report-name");
        batchProcessDTO.setCondition(condition);
        request.setCondition(condition);
    }

    private void configureFilterBatch(TaskCenterBatchRequest request) {
        Map<String, List<String>> filters = new HashMap<>();
        BaseCondition condition = new BaseCondition();
        filters.put("triggerMode", List.of("MANUAL"));
        condition.setFilter(filters);
        request.setCondition(condition);
    }

    private void doStop(String search, String url, boolean isSelectAll) throws Exception {
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        switch (search) {
            case "KEYWORD" -> configureKeywordBatch(request);
            case "FILTER" -> configureFilterBatch(request);
            default -> {
            }
        }
        if (isSelectAll) {
            request.setSelectAll(true);
        } else {
            request.setSelectIds(List.of("test-plan-report-id-1", "test-plan-report-id-2"));
        }
        request.setExcludeIds(List.of("test-plan-report-id-3", "test-plan-report-id-4"));

        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID)
                .content(JSON.toJSONString(request))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
    }

    public void testInsertData() {
        TestPlanReportApiCase testPlanReportApiCase = new TestPlanReportApiCase();
        testPlanReportApiCase.setId("task-report-id0");
        testPlanReportApiCase.setTestPlanCollectionId("task-report-id1");
        testPlanReportApiCase.setTestPlanReportId("test-plan-report-id-1-1");
        testPlanReportApiCase.setTestPlanApiCaseId("task-api-resource-id0");
        testPlanReportApiCase.setApiCaseId("apiCaseId");
        testPlanReportApiCase.setApiCaseNum(1L);
        testPlanReportApiCase.setPos(1L);
        testPlanReportApiCase.setApiCaseName("apiCaseName");
        testPlanReportApiCase.setApiCaseExecuteReportId("plan-task-report-id1");
        testPlanReportApiCaseMapper.insertSelective(testPlanReportApiCase);
        testPlanReportApiCase.setId("task-report-id1");
        testPlanReportApiCase.setTestPlanCollectionId("task-report-id1");
        testPlanReportApiCase.setTestPlanReportId("test-plan-report-id-1-2");
        testPlanReportApiCase.setTestPlanApiCaseId("task-api-resource-id0");
        testPlanReportApiCase.setApiCaseId("apiCaseId");
        testPlanReportApiCase.setApiCaseNum(1L);
        testPlanReportApiCase.setPos(1L);
        testPlanReportApiCase.setApiCaseName("apiCaseName");
        testPlanReportApiCaseMapper.insertSelective(testPlanReportApiCase);


        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("plan-task-report-id" + i);
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setTestPlanCaseId("task-test-plan-case-id" + i);
            apiReport.setName("task-report-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("100001100001");
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            apiReport.setPlan(true);
            if (i % 2 == 0) {
                apiReport.setTestPlanCaseId("task-api-resource-id" + i);
                apiReport.setStatus(ExecStatus.PENDING.name());
            } else {
                apiReport.setStatus(ExecStatus.RUNNING.name());
            }
            apiReport.setTriggerMode("MANUAL");
            reports.add(apiReport);
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiTestCaseId("task-api-resource-id" + i);
            record.setApiReportId(apiReport.getId());
            records.add(record);
        }
        apiReportService.insertApiReport(reports, records);

        TestPlanReportApiScenario testPlanApiScenario = new TestPlanReportApiScenario();
        testPlanApiScenario.setId("task-report-id0");
        testPlanApiScenario.setTestPlanCollectionId("task-report-id1");
        testPlanApiScenario.setApiScenarioId("test-plan-report-id-1-1");
        testPlanApiScenario.setApiScenarioExecuteReportId("plan-task-report-id1");
        testPlanApiScenario.setTestPlanReportId("test-plan-report-id-1-1");
        testPlanApiScenario.setApiScenarioName("apiScenarioName");
        testPlanApiScenario.setTestPlanApiScenarioId("task-api-resource-id0");
        testPlanApiScenario.setPos(1L);
        testPlanApiScenario.setApiScenarioNum(1L);
        testPlanReportApiScenarioMapper.insertSelective(testPlanApiScenario);
        List<ApiScenarioReport> scenarioReports = new ArrayList<>();
        List<ApiScenarioRecord> scenarioRecords = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("plan-task-report-id" + i);
            scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
            scenarioReport.setTestPlanScenarioId("task-test-plan-case-id" + i);
            scenarioReport.setName("task-report-name" + i);
            scenarioReport.setStartTime(System.currentTimeMillis());
            scenarioReport.setCreateUser("admin");
            scenarioReport.setUpdateUser("admin");
            if (i % 2 == 0) {
                scenarioReport.setTestPlanScenarioId("task-api-resource-id" + i);
                scenarioReport.setStatus(ExecStatus.PENDING.name());
            } else {
                scenarioReport.setStatus(ExecStatus.RUNNING.name());
            }
            scenarioReport.setUpdateTime(System.currentTimeMillis());
            scenarioReport.setPoolId("100001100001");
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("task-MANUAL");
            scenarioReport.setPlan(true);
            scenarioReports.add(scenarioReport);
            ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
            scenarioRecord.setApiScenarioId("task-api-resource-id" + i);
            scenarioRecord.setApiScenarioReportId(scenarioReport.getId());
            scenarioRecords.add(scenarioRecord);
        }
        apiScenarioReportService.insertApiScenarioReport(scenarioReports, scenarioRecords);

    }

}
