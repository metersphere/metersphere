package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiScenarioRecord;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiTestCaseRecord;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.TaskCenterResourceType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePoolBlob;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourceNodeDTO;
import io.metersphere.system.dto.pool.TestResourcePoolDTO;
import io.metersphere.sdk.dto.BaseCondition;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskcenter.request.TaskCenterBatchRequest;
import io.metersphere.system.dto.taskcenter.request.TaskCenterPageRequest;
import io.metersphere.system.mapper.TestResourcePoolBlobMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import io.metersphere.system.utils.Pager;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
public class ApiTaskCenterControllerTests extends BaseTest {

    private static final String BASE_PATH = "/task/center/api/";
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
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private TestResourcePoolBlobMapper testResourcePoolBlobMapper;
    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;


    @Test
    @Order(9)
    @Sql(scripts = {"/dml/init_api_definition.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getPage() throws Exception {

        doTaskCenterPage("KEYWORD", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPage("FILTER", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_ORG_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPage("FILTER", REAL_TIME_ORG_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPage("FILTER", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.API_CASE.toString());

        doTaskCenterPage("KEYWORD", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPage("FILTER", REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_ORG_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPage("FILTER", REAL_TIME_ORG_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPage("KEYWORD", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
        doTaskCenterPage("FILTER", REAL_TIME_SYSTEM_PAGE, TaskCenterResourceType.API_SCENARIO.toString());
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
        filters.put("triggerMode", List.of("MANUAL"));
        request.setFilter(filters);
    }

    @Test
    @Order(10)
    public void getPageError() throws Exception {
        doTaskCenterPageError(REAL_TIME_PROJECT_PAGE, TaskCenterResourceType.API_CASE.toString());
        doTaskCenterPageError(REAL_TIME_ORG_PAGE, TaskCenterResourceType.API_CASE.toString());

    }

    @Test
    @Order(11)
    public void stop() throws Exception {
        testInsertData();
        doStop("KEYWORD", REAL_TIME_PROJECT_STOP, TaskCenterResourceType.API_CASE.toString(), true);
        doStop("FILTER", REAL_TIME_PROJECT_STOP, TaskCenterResourceType.API_CASE.toString(), false);
        doStop("KEYWORD", REAL_TIME_ORG_STOP, TaskCenterResourceType.API_CASE.toString(), true);
        doStop("FILTER", REAL_TIME_ORG_STOP, TaskCenterResourceType.API_CASE.toString(), false);
        doStop("KEYWORD", REAL_TIME_SYSTEM_STOP, TaskCenterResourceType.API_CASE.toString(), true);
        doStop("FILTER", REAL_TIME_SYSTEM_STOP, TaskCenterResourceType.API_CASE.toString(), false);
        // scenario
        doStop("KEYWORD", REAL_TIME_PROJECT_STOP, TaskCenterResourceType.API_SCENARIO.toString(), true);
        doStop("FILTER", REAL_TIME_PROJECT_STOP, TaskCenterResourceType.API_SCENARIO.toString(), false);
        doStop("KEYWORD", REAL_TIME_ORG_STOP, TaskCenterResourceType.API_SCENARIO.toString(), true);
        doStop("FILTER", REAL_TIME_ORG_STOP, TaskCenterResourceType.API_SCENARIO.toString(), false);
        doStop("KEYWORD", REAL_TIME_SYSTEM_STOP, TaskCenterResourceType.API_SCENARIO.toString(), true);
        doStop("FILTER", REAL_TIME_SYSTEM_STOP, TaskCenterResourceType.API_SCENARIO.toString(), false);

    }

    @Test
    @Order(12)
    public void stopById() throws Exception {
        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/api/project/stop/API_CASE/task-report-id0")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/api/org/stop/API_SCENARIO/task-report-id0")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.get("/task/center/api/system/stop/API_SCENARIO/task-report-id0")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                        .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    public void testInsertData() {
        List<ApiReport> reports = new ArrayList<>();
        List<ApiTestCaseRecord> records = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiReport apiReport = new ApiReport();
            apiReport.setId("task-report-id" + i);
            apiReport.setProjectId(DEFAULT_PROJECT_ID);
            apiReport.setName("task-report-name" + i);
            apiReport.setStartTime(System.currentTimeMillis());
            apiReport.setCreateUser("admin");
            apiReport.setUpdateUser("admin");
            apiReport.setUpdateTime(System.currentTimeMillis());
            apiReport.setPoolId("api-pool-id");
            apiReport.setEnvironmentId("api-environment-id" + i);
            apiReport.setRunMode("api-run-mode" + i);
            if (i % 2 == 0) {
                apiReport.setTestPlanCaseId("task-api-resource-id" + i);
                apiReport.setStatus(ExecStatus.PENDING.name());
            } else {
                apiReport.setStatus(ExecStatus.RUNNING.name());
            }
            apiReport.setTriggerMode("task-MANUAL");
            reports.add(apiReport);
            ApiTestCaseRecord record = new ApiTestCaseRecord();
            record.setApiTestCaseId("task-api-resource-id" + i);
            record.setApiReportId(apiReport.getId());
            records.add(record);
        }
        apiReportService.insertApiReport(reports, records);

        List<ApiScenarioReport> scenarioReports = new ArrayList<>();
        List<ApiScenarioRecord> scenarioRecords = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiScenarioReport scenarioReport = new ApiScenarioReport();
            scenarioReport.setId("task-report-id" + i);
            scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
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
            scenarioReport.setPoolId("api-pool-id");
            scenarioReport.setEnvironmentId("api-environment-id" + i);
            scenarioReport.setRunMode("api-run-mode" + i);
            scenarioReport.setTriggerMode("task-MANUAL");
            scenarioReports.add(scenarioReport);
            ApiScenarioRecord scenarioRecord = new ApiScenarioRecord();
            scenarioRecord.setApiScenarioId("task-api-resource-id" + i);
            scenarioRecord.setApiScenarioReportId(scenarioReport.getId());
            scenarioRecords.add(scenarioRecord);
        }
        apiScenarioReportService.insertApiScenarioReport(scenarioReports, scenarioRecords);

        TestResourcePoolDTO testResourcePool = new TestResourcePoolDTO();
        testResourcePool.setId("api-pool-id");
        testResourcePool.setCreateTime(System.currentTimeMillis());
        testResourcePool.setUpdateTime(System.currentTimeMillis());
        testResourcePool.setDeleted(false);
        testResourcePool.setName("api test");
        testResourcePool.setCreateUser("admin");
        testResourcePool.setAllOrg(true);
        testResourcePool.setEnable(true);
        testResourcePool.setType(ResourcePoolTypeEnum.NODE.getName());
        TestResourcePoolBlob testResourcePoolBlob = new TestResourcePoolBlob();
        testResourcePoolBlob.setId("api-pool-id");
        TestResourceDTO testResourceDTO = new TestResourceDTO();
        TestResourceNodeDTO testResourceNodeDTO = new TestResourceNodeDTO();
        testResourceNodeDTO.setIp(host);
        testResourceNodeDTO.setPort(port + StringUtils.EMPTY);
        testResourceNodeDTO.setConcurrentNumber(10);
        testResourceDTO.setNodesList(List.of(testResourceNodeDTO));
        String configuration = JSON.toJSONString(testResourceDTO);
        testResourcePoolBlob.setConfiguration(configuration.getBytes());
        testResourcePool.setTestResourceDTO(testResourceDTO);
        testResourcePoolMapper.insert(testResourcePool);
        testResourcePoolBlobMapper.insert(testResourcePoolBlob);

        baseResourcePoolTestService.insertResourcePoolProject(testResourcePool);
        baseResourcePoolTestService.insertProjectApplication(testResourcePool);
        
    }


    private void doStop(String search, String url, String moduleType, boolean isSelectAll) throws Exception {
        TaskCenterBatchRequest request = new TaskCenterBatchRequest();
        request.setModuleType(moduleType);
        switch (search) {
            case "KEYWORD" -> configureKeywordBatch(request);
            case "FILTER" -> configureFilterBatch(request);
            default -> {
            }
        }
        if (isSelectAll) {
            request.setSelectAll(true);
        } else {
            request.setSelectIds(List.of("task-report-id0", "task-report-id1"));
        }
        request.setExcludeIds(List.of("task-report-id3", "task-report-id4"));

        mockPost("/api/stop", "");
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID)
                .header(SessionConstants.CURRENT_ORGANIZATION, DEFAULT_ORGANIZATION_ID)
                .content(JSON.toJSONString(request))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
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
        filters.put("triggerMode", List.of("task-MANUAL"));
        condition.setFilter(filters);
        request.setCondition(condition);
    }


}
