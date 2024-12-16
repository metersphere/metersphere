package io.metersphere.plan.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiRunModeRequest;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiReportDTO;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.mapper.ApiReportDetailMapper;
import io.metersphere.api.mapper.ApiReportLogMapper;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiReportService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.domain.TestPlanApiCaseExample;

import io.metersphere.plan.service.TestPlanApiCaseExecuteCallbackService;
import io.metersphere.sdk.constants.*;
import io.metersphere.system.dto.ModuleSelectDTO;
import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanApiCaseControllerTests extends BaseTest {

    private static final String BASE_PATH = "/test-plan/api/case/";
    public static final String API_CASE_PAGE = "page";
    public static final String API_CASE_TREE_COUNT = "module/count";
    public static final String API_CASE_TREE_MODULE_TREE = "tree";
    public static final String API_CASE_DISASSOCIATE = "disassociate";
    public static final String API_CASE_BATCH_DISASSOCIATE = "batch/disassociate";
    public static final String API_CASE_BATCH_UPDATE_EXECUTOR_URL = "batch/update/executor";
    private static final String URL_POST_RESOURCE_API_CASE_SORT = "/sort";
    public static final String RUN = "run/{0}";
    public static final String BATCH_RUN = "batch/run";
    public static final String RUN_WITH_REPORT_ID = "run/{0}?reportId={1}";
    private static final String API_CASE_BATCH_MOVE = "/batch/move";
    private static final String BUG_ASSOCIATE_PAGE = "/associate/bug/page";
    private static final String ASSOCIATE_BUG = "/associate/bug";
    private static final String DISASSOCIATE_BUG = "/disassociate/bug/{0}";
    public static final String API_CASE_BATCH_ADD_BUG_URL = "/batch/add-bug";
    public static final String API_CASE_BATCH_ASSOCIATE_BUG_URL = "/batch/associate-bug";

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiCaseExecuteCallbackService testPlanApiCaseExecuteCallbackService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ApiReportService apiReportService;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private ApiReportLogMapper apiReportLogMapper;
    @Resource
    private ApiReportDetailMapper apiReportDetailMapper;

    private static ApiTestCase apiTestCase;
    private static TestPlanApiCase testPlanApiCase;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_api_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testApiCasePageList() throws Exception {
        TestPlanApiCaseRequest request = new TestPlanApiCaseRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("wxxx_1");
        request.setProjectId("wxx_1234");
        this.requestPost(API_CASE_PAGE, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPost(API_CASE_PAGE, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(2)
    public void testApiCaseCount() throws Exception {
        TestPlanApiCaseModuleRequest request = new TestPlanApiCaseModuleRequest();
        request.setTestPlanId("wxxx_1");
        request.setProjectId("wxx_1234");
        request.setProtocols(List.of("HTTP"));
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTreeType("MODULE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_TREE_COUNT, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTreeType("COLLECTION");
        this.requestPostWithOkAndReturn(API_CASE_TREE_COUNT, request);

        this.testSort();
    }

    public void testSort() throws Exception {
        TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
        testPlanApiCaseExample.createCriteria().andTestPlanCollectionIdEqualTo("wxxx_2");
        testPlanApiCaseExample.setOrderByClause("pos asc");
        List<TestPlanApiCase> apiList = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);

        //最后一个移动到第一位之前
        ResourceSortRequest request = new ResourceSortRequest();
        request.setTestCollectionId("wxxx_2");
        request.setProjectId("wxx_1234");
        request.setMoveId(apiList.getLast().getId());
        request.setTargetId(apiList.getFirst().getId());
        request.setMoveMode(MoveTypeEnum.BEFORE.name());

        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_SORT, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanOperationResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanOperationResponse.class);
        Assertions.assertEquals(response.getOperationCount(), 1);
        apiList = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
        Assertions.assertEquals(apiList.getFirst().getId(), request.getMoveId());
        Assertions.assertEquals(apiList.get(1).getId(), request.getTargetId());

        //将这时的第30个放到第一位之后
        request.setTargetId(apiList.getLast().getId());
        request.setMoveId(apiList.getFirst().getId());
        request.setMoveMode(MoveTypeEnum.AFTER.name());
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_CASE_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanOperationResponse.class);
        Assertions.assertEquals(response.getOperationCount(), 1);
        apiList = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
        Assertions.assertEquals(apiList.getFirst().getId(), request.getTargetId());
        Assertions.assertEquals(apiList.get(1).getId(), request.getMoveId());

    }

    @Test
    @Order(3)
    public void testApiCaseModuleTree() throws Exception {
        TestPlanApiCaseTreeRequest request = new TestPlanApiCaseTreeRequest();
        request.setTestPlanId("wxxx_1");
        request.setTreeType("MODULE");
        this.requestPostWithOkAndReturn(API_CASE_TREE_MODULE_TREE, request);
        request.setTestPlanId("wxxx_2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_TREE_MODULE_TREE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTestPlanId("wxxx_2");
        request.setTreeType("COLLECTION");
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(API_CASE_TREE_MODULE_TREE, request);
        String returnData1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder1 = JSON.parseObject(returnData1, ResultHolder.class);
        Assertions.assertNotNull(resultHolder1);
    }


    @Test
    @Order(4)
    public void testApiCaseDisassociate() throws Exception {
        TestPlanDisassociationRequest request = new TestPlanDisassociationRequest();
        request.setTestPlanId("wxxx_2");
        request.setId("wxxx_3");

        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_DISASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(5)
    public void testBatchUpdateExecutor() throws Exception {
        TestPlanApiCaseUpdateRequest request = new TestPlanApiCaseUpdateRequest();
        request.setUserId("test_user");
        request.setTestPlanId("wxxx_2");
        request.setSelectAll(true);
        this.requestPostWithOk(API_CASE_BATCH_UPDATE_EXECUTOR_URL, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOk(API_CASE_BATCH_UPDATE_EXECUTOR_URL, request);
        request.setTestPlanId("wxxx_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("wxxx_1"));
        this.requestPostWithOk(API_CASE_BATCH_UPDATE_EXECUTOR_URL, request);
    }


    @Test
    @Order(6)
    public void testApiCaseBatchDisassociate() throws Exception {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        request.setTestPlanId("wxxx_2");
        request.setSelectAll(true);
        this.requestPostWithOkAndReturn(API_CASE_BATCH_DISASSOCIATE, request);
        request.setProtocols(List.of("HTTP"));
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_BATCH_DISASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(6)
    public void testApiCaseAssociate() {
        // api
        Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates = new HashMap<>();
        List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests = new ArrayList<>();
        BaseCollectionAssociateRequest baseCollectionAssociateRequest = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest.setCollectionId("wxxx_2");
        baseCollectionAssociateRequest.setModules(buildModulesAll(AssociateCaseType.API));
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        collectionAssociates.put(AssociateCaseType.API, baseCollectionAssociateRequests);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        userDTO.setLastOrganizationId("wxx_1234");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);
        testPlanApiCaseService.associateCollection("wxxx_1", collectionAssociates, user);

        baseCollectionAssociateRequest.setModules(buildModules(AssociateCaseType.API));
        testPlanApiCaseService.associateCollection("wxxx_1", collectionAssociates, user);

        //api case
        Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates1 = new HashMap<>();
        List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests1 = new ArrayList<>();
        BaseCollectionAssociateRequest baseCollectionAssociateRequest1 = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest1.setCollectionId("wxxx_2");
        baseCollectionAssociateRequest1.setModules(buildModulesAll(AssociateCaseType.API_CASE));
        baseCollectionAssociateRequests1.add(baseCollectionAssociateRequest1);
        collectionAssociates1.put(AssociateCaseType.API_CASE, baseCollectionAssociateRequests1);
        testPlanApiCaseService.associateCollection("wxxx_1", collectionAssociates1, user);

        baseCollectionAssociateRequest1.setModules(buildModules(AssociateCaseType.API_CASE));
        testPlanApiCaseService.associateCollection("wxxx_1", collectionAssociates1, user);

        apiTestCase = initApiData();
        TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
        testPlanApiCase.setApiCaseId(apiTestCase.getId());
        testPlanApiCase.setTestPlanId("wxxx_1");
        testPlanApiCase.setTestPlanCollectionId("wxxx_1");
        testPlanApiCase.setId(UUID.randomUUID().toString());
        testPlanApiCase.setCreateTime(System.currentTimeMillis());
        testPlanApiCase.setCreateUser("admin");
        testPlanApiCase.setPos(0L);
        testPlanApiCaseMapper.insert(testPlanApiCase);
        this.testPlanApiCase = testPlanApiCase;
        // todo 关联的接口测试
    }

    private TestPlanCollectionAssociateDTO buildModules(String type) {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(false);
        associateDTO.setAssociateType(type);
        associateDTO.setProjectId("wxx_1234");
        associateDTO.setModuleMaps(buildModuleMap());
        associateDTO.setProtocols(List.of("HTTP"));
        return associateDTO;
    }

    private Map<String, ModuleSelectDTO> buildModuleMap() {
        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(false);
        moduleSelectDTO.setSelectIds(List.of("wxxx_api_1"));
        moduleMap.put("123", moduleSelectDTO);
        return moduleMap;
    }

    private TestPlanCollectionAssociateDTO buildModulesAll(String type) {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(true);
        associateDTO.setAssociateType(type);
        associateDTO.setProjectId("wxx_1234");
        associateDTO.setProtocols(List.of("HTTP"));

        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(true);
        moduleSelectDTO.setSelectIds(new ArrayList<>());
        moduleMap.put("123", moduleSelectDTO);
        associateDTO.setModuleMaps(moduleMap);
        return associateDTO;
    }

    @Test
    @Order(7)
    public void run() throws Exception {
        assertRun(this.requestGetAndReturn(RUN, testPlanApiCase.getId()));
        assertRun(this.requestGetAndReturn(RUN_WITH_REPORT_ID, testPlanApiCase.getId(), "reportId"));
        assertErrorCode(this.requestGet(RUN, "11"), NOT_FOUND);
        GetRunScriptRequest request = new GetRunScriptRequest();
        TaskItem taskItem = new TaskItem();
        taskItem.setId(UUID.randomUUID().toString());
        taskItem.setResourceId(testPlanApiCase.getId());
        taskItem.setReportId("reportId");
        request.setTaskItem(taskItem);
        request.setPoolId("poolId");
        request.setUserId(InternalUser.ADMIN.getValue());
        request.setTriggerMode(TaskTriggerMode.MANUAL.name());
        request.setRunMode(ApiExecuteRunMode.RUN.name());
        testPlanApiCaseExecuteCallbackService.getRunScript(request);

        requestGetPermissionTest(PermissionConstants.TEST_PLAN_READ_EXECUTE, RUN, testPlanApiCase.getId());
    }

    public static void assertRun(MvcResult mvcResult) throws UnsupportedEncodingException {
        Map resultData = JSON.parseMap(mvcResult.getResponse().getContentAsString());
        Integer code = (Integer) resultData.get("code");
        if (code != ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR.getCode() && code != ApiResultCode.EXECUTE_RESOURCE_POOL_NOT_CONFIG.getCode()
                && code != ApiResultCode.TASK_ERROR_MESSAGE_INVALID_RESOURCE_POOL.getCode()) {
            Assertions.assertTrue(false);
        }
    }

    @Test
    @Order(7)
    public void batchRun() throws Exception {
        TestPlanApiCaseBatchRunRequest request = new TestPlanApiCaseBatchRunRequest();
        request.setSelectIds(List.of(testPlanApiCase.getId()));
        request.setTestPlanId(testPlanApiCase.getTestPlanId());
        ApiRunModeRequest apiRunModeRequest = new ApiRunModeRequest();
        apiRunModeRequest.setRunMode(ApiBatchRunMode.PARALLEL.name());
        apiRunModeRequest.setStopOnFailure(false);
        apiRunModeRequest.setPoolId("poolId");
        this.requestPostWithOk(BATCH_RUN, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setIntegratedReport(false);
        apiRunModeRequest.setStopOnFailure(true);
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setRunMode(ApiBatchRunMode.SERIAL.name());
        this.requestPostWithOk(BATCH_RUN, request);

        apiRunModeRequest.setIntegratedReport(true);
        this.requestPostWithOk(BATCH_RUN, request);

        // @@校验权限
        requestPostPermissionTest(PermissionConstants.TEST_PLAN_READ_EXECUTE, BATCH_RUN, request);
    }

    public ApiTestCase initApiData() {
        ApiDefinitionAddRequest apiDefinitionAddRequest = createApiDefinitionAddRequest();
        MsHTTPElement msHttpElement = new MsHTTPElement();
        msHttpElement.setPath("/test");
        msHttpElement.setMethod("GET");
        msHttpElement.setName("name");
        msHttpElement.setEnable(true);
        Body body = new Body();
        body.setBodyType(Body.BodyType.RAW.name());
        body.setRawBody(new RawBody());
        msHttpElement.setBody(body);
        apiDefinitionAddRequest.setRequest(getTestElementParam(msHttpElement));
        ApiDefinition apiDefinition = apiDefinitionService.create(apiDefinitionAddRequest, "admin");
        apiDefinitionAddRequest.setName(UUID.randomUUID().toString());

        ApiTestCaseAddRequest caseAddRequest = new ApiTestCaseAddRequest();
        caseAddRequest.setApiDefinitionId(apiDefinition.getId());
        caseAddRequest.setName(UUID.randomUUID().toString());
        caseAddRequest.setProjectId(DEFAULT_PROJECT_ID);
        caseAddRequest.setPriority("P0");
        caseAddRequest.setStatus(ApiDefinitionStatus.PROCESSING.name());
        caseAddRequest.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        caseAddRequest.setEnvironmentId("envId");
        caseAddRequest.setRequest(getTestElementParam(msHttpElement));
        return apiTestCaseService.addCase(caseAddRequest, "admin");
    }

    public static ApiDefinitionAddRequest createApiDefinitionAddRequest() {
        ExtBaseProjectVersionMapper extBaseProjectVersionMapper = CommonBeanFactory.getBean(ExtBaseProjectVersionMapper.class);
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID);
        ApiDefinitionAddRequest request = new ApiDefinitionAddRequest();
        request.setName(UUID.randomUUID().toString());
        request.setProtocol(ApiConstants.HTTP_PROTOCOL);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setMethod("POST");
        request.setPath("/api/admin/posts");
        request.setStatus(ApiDefinitionStatus.PROCESSING.name());
        request.setModuleId("default");
        request.setVersionId(defaultVersion);
        request.setDescription("描述内容");
        request.setTags(new LinkedHashSet<>(List.of("tag1", "tag2")));
        request.setCustomFields(List.of());
        return request;
    }


    private Object getTestElementParam(MsHTTPElement msHttpElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHttpElement));
    }


    @Test
    @Order(10)
    public void testGet() throws Exception {

        List<ApiReport> reports = new ArrayList<>();
        ApiReport apiReport = new ApiReport();
        apiReport.setId("plan-test-report-id");
        apiReport.setProjectId(DEFAULT_PROJECT_ID);
        apiReport.setName("plan-test-report-name");
        apiReport.setStartTime(System.currentTimeMillis());
        apiReport.setCreateUser("admin");
        apiReport.setUpdateUser("admin");
        apiReport.setUpdateTime(System.currentTimeMillis());
        apiReport.setPoolId("resource_pool");
        apiReport.setEnvironmentId("test-env");
        apiReport.setRunMode("api-run-mode");
        apiReport.setTestPlanCaseId("test-plan-case-id");
        apiReport.setStatus(ResultStatus.SUCCESS.name());
        apiReport.setTriggerMode("api-trigger-mode");
        apiReport.setIntegrated(true);
        reports.add(apiReport);
        ApiTestCaseRecord record = new ApiTestCaseRecord();
        record.setApiTestCaseId("api-resource-id");
        record.setApiReportId(apiReport.getId());
        apiReportService.insertApiReport(reports, List.of(record));
        List<ApiReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ApiReportStep apiReportStep = new ApiReportStep();
            apiReportStep.setStepId("plan-test-report-step-id" + i);
            apiReportStep.setReportId("plan-test-report-id");
            apiReportStep.setSort((long) i);
            apiReportStep.setStepType(ApiExecuteResourceType.API_CASE.name());
            steps.add(apiReportStep);
        }

        apiReportService.insertApiReportStep(steps);
        ApiReportLog apiReportLog = new ApiReportLog();
        apiReportLog.setReportId("plan-test-report-id");
        apiReportLog.setId(IDGenerator.nextStr());
        apiReportLog.setConsole("test-console".getBytes());
        apiReportLogMapper.insert(apiReportLog);

        MvcResult mvcResult = this.requestGetWithOk("/report/get/plan-test-report-id")
                .andReturn();
        ApiReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiReportDTO.class);

        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "plan-test-report-id");
        ApiReport apiReport1 = apiReportMapper.selectByPrimaryKey("plan-test-report-id");
        apiReport1.setTestPlanCaseId("NONE");
        apiReportMapper.updateByPrimaryKey(apiReport1);

        mockMvc.perform(getRequestBuilder("/report/get/plan-test-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());

        apiReport1.setTestPlanCaseId("test-plan-case-id");
        apiReportMapper.updateByPrimaryKey(apiReport1);


        // @@校验权限
        requestGetPermissionTest(PermissionConstants.TEST_PLAN_REPORT_READ, "/report/get/plan-test-report-id");

        List<ApiReportDetail> reportsDetail = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ApiReportDetail apiReportDetail = new ApiReportDetail();
            apiReportDetail.setId("test-report-detail-id" + i);
            apiReportDetail.setReportId("plan-test-report-id");
            apiReportDetail.setStepId("plan-test-report-step-id1");
            apiReportDetail.setStatus("success");
            apiReportDetail.setResponseSize(0L);
            apiReportDetail.setRequestTime((long) i);
            apiReportDetail.setContent("{\"resourceId\":\"\",\"stepId\":null,\"threadName\":\"Thread Group\",\"name\":\"HTTP Request1\",\"url\":\"https://www.baidu.com/\",\"requestSize\":195,\"startTime\":1705570589125,\"endTime\":1705570589310,\"error\":1,\"headers\":\"Connection: keep-alive\\nContent-Length: 0\\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8\\nHost: www.baidu.com\\nUser-Agent: Apache-HttpClient/4.5.14 (Java/21)\\n\",\"cookies\":\"\",\"body\":\"POST https://www.baidu.com/\\n\\nPOST data:\\n\\n\\n[no cookies]\\n\",\"status\":\"ERROR\",\"method\":\"POST\",\"assertionTotal\":1,\"passAssertionsTotal\":0,\"subRequestResults\":[],\"responseResult\":{\"responseCode\":\"200\",\"responseMessage\":\"OK\",\"responseTime\":185,\"latency\":180,\"responseSize\":2559,\"headers\":\"HTTP/1.1 200 OK\\nContent-Length: 2443\\nContent-Type: text/html\\nServer: bfe\\nDate: Thu, 18 Jan 2024 09:36:29 GMT\\n\",\"body\":\"<!DOCTYPE html>\\r\\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\\\"bg s_ipt_wr\\\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus=autofocus></span><span class=\\\"bg s_btn_wr\\\"><input type=submit id=su value=百度一下 class=\\\"bg s_btn\\\" autofocus></span> </form> </div> </div> <div id=u1> <a href=http://news.baidu.com name=tj_trnews class=mnav>新闻</a> <a href=https://www.hao123.com name=tj_trhao123 class=mnav>hao123</a> <a href=http://map.baidu.com name=tj_trmap class=mnav>地图</a> <a href=http://v.baidu.com name=tj_trvideo class=mnav>视频</a> <a href=http://tieba.baidu.com name=tj_trtieba class=mnav>贴吧</a> <noscript> <a href=http://www.baidu.com/bdorz/login.gif?login&amp;tpl=mn&amp;u=http%3A%2F%2Fwww.baidu.com%2f%3fbdorz_come%3d1 name=tj_login class=lb>登录</a> </noscript> <script>document.write('<a href=\\\"http://www.baidu.com/bdorz/login.gif?login&tpl=mn&u='+ encodeURIComponent(window.location.href+ (window.location.search === \\\"\\\" ? \\\"?\\\" : \\\"&\\\")+ \\\"bdorz_come=1\\\")+ '\\\" name=\\\"tj_login\\\" class=\\\"lb\\\">登录</a>');\\r\\n                </script> <a href=//www.baidu.com/more/ name=tj_briicon class=bri style=\\\"display: block;\\\">更多产品</a> </div> </div> </div> <div id=ftCon> <div id=ftConw> <p id=lh> <a href=http://home.baidu.com>关于百度</a> <a href=http://ir.baidu.com>About Baidu</a> </p> <p id=cp>&copy;2017&nbsp;Baidu&nbsp;<a href=http://www.baidu.com/duty/>使用百度前必读</a>&nbsp; <a href=http://jianyi.baidu.com/ class=cp-feedback>意见反馈</a>&nbsp;京ICP证030173号&nbsp; <img src=//www.baidu.com/img/gs.gif> </p> </div> </div> </div> </body> </html>\\r\\n\",\"contentType\":\"text/html\",\"vars\":null,\"imageUrl\":null,\"socketInitTime\":14,\"dnsLookupTime\":0,\"tcpHandshakeTime\":0,\"sslHandshakeTime\":0,\"transferStartTime\":166,\"downloadTime\":5,\"bodySize\":2443,\"headerSize\":116,\"assertions\":[{\"name\":\"JSON Assertion\",\"content\":null,\"script\":null,\"message\":\"Expected to find an object with property ['test'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.\",\"pass\":false}]},\"isSuccessful\":false,\"fakeErrorMessage\":\"\",\"fakeErrorCode\":null}\n".getBytes());
            reportsDetail.add(apiReportDetail);
        }
        apiReportDetailMapper.batchInsert(reportsDetail);

        this.requestGetWithOk("/report/get/detail/plan-test-report-id" + "/" + "plan-test-report-step-id1")
                .andReturn();
        requestGetPermissionTest(PermissionConstants.TEST_PLAN_REPORT_READ, "/report/get/detail/plan-test-report-id" + "/" + "plan-test-report-step-id1");
    }

    @Test
    @Order(11)
    public void testApiCaseBatchMove() throws Exception {
        TestPlanApiCaseBatchMoveRequest request = new TestPlanApiCaseBatchMoveRequest();
        request.setTestPlanId("wxxx_1");
        request.setTargetCollectionId("wxxx_2");
        request.setSelectAll(true);
        this.requestPostWithOk(API_CASE_BATCH_MOVE, request);
    }

    @Test
    @Order(12)
    void testAssociateBugPage() throws Exception {
        BugPageProviderRequest request = new BugPageProviderRequest();
        request.setSourceId("test_plan_case_id");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(BUG_ASSOCIATE_PAGE, request);
    }

    @Test
    @Order(13)
    void testAssociateBug() throws Exception {
        TestPlanCaseAssociateBugRequest request = new TestPlanCaseAssociateBugRequest();
        request.setTestPlanCaseId("test_plan_case_id");
        request.setCaseId("case_id");
        request.setTestPlanId("test_plan_id");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setSelectAll(false);
        request.setSelectIds(List.of("oasis"));
        this.requestPost(ASSOCIATE_BUG, request);
    }

    @Test
    @Order(14)
    void testDisassociateBug() throws Exception {
        this.requestGet(DISASSOCIATE_BUG, "test_plan_case_id");
    }

    @Test
    @Order(15)
    public void testBatchAddBug() throws Exception {
        TestPlanCaseBatchAddBugRequest request = buildRequest(false);
        List<MockMultipartFile> files = new ArrayList<>();
        LinkedMultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(API_CASE_BATCH_ADD_BUG_URL, paramMap);

        request.setSelectAll(true);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(API_CASE_BATCH_ADD_BUG_URL, paramMap);
    }

    private TestPlanCaseBatchAddBugRequest buildRequest(boolean isUpdate) {
        TestPlanCaseBatchAddBugRequest request = new TestPlanCaseBatchAddBugRequest();
        request.setTestPlanId("wxxx_1");
        request.setProjectId("wxx_1234");
        request.setTitle("default-bug-title");
        request.setDescription("default-bug-description");
        request.setTemplateId("default-bug-template");
        request.setLinkFileIds(List.of("default-bug-file-id-1"));
        if (isUpdate) {
            request.setId("default-bug-id");
            request.setUnLinkRefIds(List.of("default-file-association-id"));
            request.setDeleteLocalFileIds(List.of("default-bug-file-id"));
            request.setLinkFileIds(List.of("default-bug-file-id-2"));
        }
        BugCustomFieldDTO fieldDTO1 = new BugCustomFieldDTO();
        fieldDTO1.setId("custom-field");
        fieldDTO1.setName("oasis");
        BugCustomFieldDTO fieldDTO2 = new BugCustomFieldDTO();
        fieldDTO2.setId("test_field");
        fieldDTO2.setName(JSON.toJSONString(List.of("test")));
        BugCustomFieldDTO handleUserField = new BugCustomFieldDTO();
        handleUserField.setId("handleUser");
        handleUserField.setName("处理人");
        handleUserField.setValue("admin");
        BugCustomFieldDTO statusField = new BugCustomFieldDTO();
        statusField.setId("status");
        statusField.setName("状态");
        statusField.setValue("1");
        request.setCustomFields(List.of(fieldDTO1, fieldDTO2, handleUserField, statusField));
        return request;
    }

    @Test
    @Order(19)
    public void testBatchAssociateBug() throws Exception {
        TestPlanCaseBatchAssociateBugRequest request = new TestPlanCaseBatchAssociateBugRequest();
        request.setBugIds(Arrays.asList("123456"));
        request.setTestPlanId("wxxx_1");
        this.requestPostWithOk(API_CASE_BATCH_ASSOCIATE_BUG_URL, request);
        request.setSelectAll(true);
        this.requestPostWithOk(API_CASE_BATCH_ASSOCIATE_BUG_URL, request);
    }
}
