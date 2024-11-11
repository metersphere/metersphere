package io.metersphere.plan.controller;

import io.metersphere.api.constants.ApiScenarioStatus;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.ApiRunModeRequest;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.dto.scenario.*;
import io.metersphere.api.mapper.ApiScenarioReportDetailBlobMapper;
import io.metersphere.api.mapper.ApiScenarioReportDetailMapper;
import io.metersphere.api.mapper.ApiScenarioReportLogMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.service.scenario.ApiScenarioReportService;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import io.metersphere.plan.enums.TestPlanStatus;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.plan.service.TestPlanService;
import io.metersphere.sdk.constants.*;
import io.metersphere.system.dto.ModuleSelectDTO;
import io.metersphere.plan.dto.TestPlanCollectionAssociateDTO;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.dto.response.TestPlanOperationResponse;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.request.BugPageProviderRequest;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskItem;
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

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanApiScenarioControllerTests extends BaseTest {

    private static final String BASE_PATH = "/test-plan/api/scenario/";
    public static final String RUN = "run/{0}";
    public static final String RUN_WITH_REPORT_ID = "run/{0}?reportId={1}";
    public static final String API_SCENARIO_PAGE = "page";
    public static final String API_SCENARIO_TREE_COUNT = "module/count";
    public static final String API_SCENARIO_TREE = "tree";
    public static final String BATCH_RUN = "batch/run";
    public static final String API_SCENARIO_DISASSOCIATE = "disassociate";
    public static final String API_SCENARIO_BATCH_DISASSOCIATE = "batch/disassociate";
    public static final String API_SCENARIO_BATCH_UPDATE_EXECUTOR_URL = "batch/update/executor";
    private static final String URL_POST_RESOURCE_API_SCENARIO_SORT = "/sort";
    private static final String API_SCENARIO_BATCH_MOVE_URL = "/batch/move";
    private static final String BUG_ASSOCIATE_PAGE = "/associate/bug/page";
    private static final String ASSOCIATE_BUG = "/associate/bug";
    private static final String DISASSOCIATE_BUG = "/disassociate/bug/{0}";
    public static final String API_SCENARIO_BATCH_ADD_BUG_URL = "/batch/add-bug";
    public static final String API_SCENARIO_BATCH_ASSOCIATE_BUG_URL = "/batch/associate-bug";

    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiScenarioReportDetailMapper apiScenarioReportDetailMapper;
    @Resource
    private ApiScenarioReportDetailBlobMapper apiScenarioReportDetailBlobMapper;
    @Resource
    private ApiScenarioReportLogMapper apiScenarioReportLogMapper;

    private static ApiScenario apiScenario;
    private static TestPlanApiScenario testPlanApiScenario;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(1)
    public void associate() {
        apiScenario = initApiData();

        TestPlan testPlan = new TestPlan();
        testPlan.setId(IDGenerator.nextStr());
        testPlan.setPos(0L);
        testPlan.setName(UUID.randomUUID().toString());
        testPlan.setProjectId(DEFAULT_PROJECT_ID);
        testPlan.setActualEndTime(System.currentTimeMillis());
        testPlan.setPlannedStartTime(System.currentTimeMillis());
        testPlan.setActualEndTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setCreateUser(InternalUser.ADMIN.getValue());
        testPlan.setUpdateUser(InternalUser.ADMIN.getValue());
        testPlan.setModuleId(UUID.randomUUID().toString());
        testPlan.setStatus(TestPlanStatus.COMPLETED.name());
        testPlan.setNum(IDGenerator.nextNum());
        testPlan.setType(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
        testPlan.setGroupId(testPlan.getId());
        testPlanMapper.insert(testPlan);

        TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
        testPlanApiScenario.setApiScenarioId(apiScenario.getId());
        testPlanApiScenario.setTestPlanId(testPlan.getId());
        testPlanApiScenario.setTestPlanCollectionId("wxxx_1");
        testPlanApiScenario.setId(UUID.randomUUID().toString());
        testPlanApiScenario.setCreateTime(System.currentTimeMillis());
        testPlanApiScenario.setCreateUser("admin");
        testPlanApiScenario.setPos(0L);
        testPlanApiScenarioMapper.insert(testPlanApiScenario);
        this.testPlanApiScenario = testPlanApiScenario;
        // todo 关联的接口测试
    }

    @Test
    @Order(2)
    public void run() throws Exception {
        TestPlanApiCaseControllerTests.assertRun(this.requestGetAndReturn(RUN, testPlanApiScenario.getId()));
        TestPlanApiCaseControllerTests.assertRun(this.requestGetAndReturn(RUN, testPlanApiScenario.getId()));
        TestPlanApiCaseControllerTests.assertRun(this.requestGetAndReturn(RUN_WITH_REPORT_ID, testPlanApiScenario.getId(), "reportId"));
        assertErrorCode(this.requestGet(RUN, "11"), NOT_FOUND);
        GetRunScriptRequest request = new GetRunScriptRequest();
        TaskItem taskItem = new TaskItem();
        taskItem.setResourceId(testPlanApiScenario.getId());
        taskItem.setReportId("reportId");
        taskItem.setId(UUID.randomUUID().toString());
        request.setTaskItem(taskItem);
        request.setPoolId("poolId");
        request.setUserId(InternalUser.ADMIN.getValue());
        request.setTriggerMode(TaskTriggerMode.MANUAL.name());
        request.setRunMode(ApiExecuteRunMode.RUN.name());
        testPlanApiScenarioService.getRunScript(request);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.TEST_PLAN_READ_EXECUTE, RUN, testPlanApiScenario.getId());
    }

    @Test
    @Order(2)
    public void batchRun() throws Exception {
        mockPost("/api/batch/run", "");

        TestPlanApiScenarioBatchRunRequest request = new TestPlanApiScenarioBatchRunRequest();
        request.setSelectIds(List.of(testPlanApiScenario.getId()));
        request.setTestPlanId(testPlanApiScenario.getTestPlanId());
        ApiRunModeRequest apiRunModeRequest = new ApiRunModeRequest();
        apiRunModeRequest.setRunMode(ApiBatchRunMode.PARALLEL.name());
        apiRunModeRequest.setIntegratedReport(true);
        apiRunModeRequest.setStopOnFailure(false);
        apiRunModeRequest.setIntegratedReportName("aaaa");
        apiRunModeRequest.setPoolId("poolId");
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

    public ApiScenario initApiData() {
        ApiScenarioAddRequest request = getApiScenarioAddRequest();

        ApiScenarioStepRequest stepRequest = new ApiScenarioStepRequest();
        stepRequest.setId(IDGenerator.nextStr());
        stepRequest.setVersionId(extBaseProjectVersionMapper.getDefaultVersion(DEFAULT_PROJECT_ID));
        stepRequest.setConfig(new HashMap<>());
        stepRequest.setEnable(true);
        stepRequest.setName(UUID.randomUUID().toString());
        stepRequest.setRefType(ApiScenarioStepRefType.DIRECT.name());
        stepRequest.setStepType(ApiScenarioStepType.CUSTOM_REQUEST.name());
        stepRequest.setProjectId(DEFAULT_PROJECT_ID);
        stepRequest.setConfig(new HashMap<>());

        List<ApiScenarioStepRequest> steps = List.of(stepRequest);
        Map<String, Object> steptDetailMap = new HashMap<>();
        steptDetailMap.put(stepRequest.getId(), getMsHttpElementParam());
        request.setSteps(steps);
        request.setStepDetails(steptDetailMap);
        request.setScenarioConfig(getScenarioConfig());

        return apiScenarioService.add(request, "admin");
    }

    public ScenarioConfig getScenarioConfig() {
        ScenarioConfig scenarioConfig = new ScenarioConfig();
        MsAssertionConfig msAssertionConfig = new MsAssertionConfig();
        MsScriptAssertion scriptAssertion = new MsScriptAssertion();
        scriptAssertion.setScript("{}");
        scriptAssertion.setName("script");
        msAssertionConfig.setAssertions(List.of(scriptAssertion));
        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setExpectedValue("200");
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setName("test");
        scenarioConfig.getAssertionConfig().getAssertions().add(responseCodeAssertion);
        ScenarioOtherConfig scenarioOtherConfig = new ScenarioOtherConfig();
        scenarioOtherConfig.setFailureStrategy(ScenarioOtherConfig.FailureStrategy.CONTINUE.name());
        scenarioOtherConfig.setEnableCookieShare(true);
        scenarioConfig.setOtherConfig(scenarioOtherConfig);
        return scenarioConfig;
    }

    public static ApiScenarioAddRequest getApiScenarioAddRequest() {
        ApiScenarioAddRequest request = new ApiScenarioAddRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setDescription("desc");
        request.setName("test name");
        request.setModuleId("default");
        request.setGrouped(false);
        request.setEnvironmentId("envID");
        request.setTags(List.of("tag1", "tag2"));
        request.setPriority("P0");
        request.setStatus(ApiScenarioStatus.COMPLETED.name());
        return request;
    }

    private Object getMsHttpElementParam() {
        MsHTTPElement msHTTPElement = new MsHTTPElement();
        msHTTPElement.setPath("/test");
        msHTTPElement.setMethod("GET");
        msHTTPElement.setName("name");
        msHTTPElement.setEnable(true);
        Body body = new Body();
        body.setBodyType(Body.BodyType.RAW.name());
        body.setRawBody(new RawBody());
        msHTTPElement.setBody(body);
        return getMsHttpElementStr(msHTTPElement);
    }

    private Object getMsHttpElementStr(MsHTTPElement msHTTPElement) {
        return JSON.parseObject(ApiDataUtils.toJSONString(msHTTPElement));
    }


    @Test
    @Order(3)
    @Sql(scripts = {"/dml/init_test_plan_api_scenario.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testApiCasePageList() throws Exception {
        TestPlanApiScenarioRequest request = new TestPlanApiScenarioRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("wxxx_plan_1");
        request.setProjectId("wxx_project_1234");
        this.requestPost(API_SCENARIO_PAGE, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_PAGE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(4)
    public void testApiCaseAssociate() {
        // api_scenario
        Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates = new HashMap<>();
        List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests = new ArrayList<>();
        BaseCollectionAssociateRequest baseCollectionAssociateRequest = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest.setCollectionId("wxxx_collection_3");
        baseCollectionAssociateRequest.setModules(buildModulesAll());
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        collectionAssociates.put(AssociateCaseType.API_SCENARIO, baseCollectionAssociateRequests);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(sessionId);
        userDTO.setName("admin");
        userDTO.setLastOrganizationId("wxx_1234");
        SessionUser user = SessionUser.fromUser(userDTO, sessionId);
        testPlanApiScenarioService.associateCollection("wxxx_plan_2", collectionAssociates, user);

        baseCollectionAssociateRequest.setModules(buildModules());
        testPlanApiScenarioService.associateCollection("wxxx_plan_2", collectionAssociates, user);
    }

    private TestPlanCollectionAssociateDTO buildModules() {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(false);
        associateDTO.setAssociateType(AssociateCaseType.API_SCENARIO);
        associateDTO.setProjectId("wxx_project_1234");
        associateDTO.setModuleMaps(buildModuleMap());
        return associateDTO;
    }

    private Map<String, ModuleSelectDTO> buildModuleMap() {
        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(false);
        moduleSelectDTO.setSelectIds(List.of("wxxx_api_scenario_1"));
        moduleMap.put("wx_scenario_module_123", moduleSelectDTO);
        return moduleMap;
    }


    private TestPlanCollectionAssociateDTO buildModulesAll() {
        TestPlanCollectionAssociateDTO associateDTO = new TestPlanCollectionAssociateDTO();
        associateDTO.setSelectAllModule(true);
        associateDTO.setAssociateType(AssociateCaseType.API_SCENARIO);
        associateDTO.setProjectId("wxx_project_1234");

        Map<String, ModuleSelectDTO> moduleMap = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(true);
        moduleSelectDTO.setSelectIds(new ArrayList<>());
        moduleMap.put("wx_scenario_module_123", moduleSelectDTO);
        associateDTO.setModuleMaps(moduleMap);
        return associateDTO;
    }

    @Test
    @Order(5)
    public void testApiScenarioBatchMove() throws Exception {
        BaseBatchMoveRequest request = new BaseBatchMoveRequest();
        request.setTestPlanId("wxxx_plan_1");
        request.setTargetCollectionId("wxxx_collection_2");
        request.setSelectAll(true);
        this.requestPostWithOk(API_SCENARIO_BATCH_MOVE_URL, request);
    }


    @Test
    @Order(4)
    public void testApiScenarioCount() throws Exception {
        TestPlanApiScenarioModuleRequest request = new TestPlanApiScenarioModuleRequest();
        request.setTestPlanId("wxxx_plan_1");
        request.setProjectId("wxx_project_1234");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTreeType("MODULE");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_TREE_COUNT, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTreeType("COLLECTION");
        this.requestPostWithOkAndReturn(API_SCENARIO_TREE_COUNT, request);

        this.testSort();
    }

    public void testSort() throws Exception {
        TestPlanApiScenarioExample testPlanApiScenarioExample = new TestPlanApiScenarioExample();
        testPlanApiScenarioExample.createCriteria().andTestPlanCollectionIdEqualTo("wxxx_collection_2");
        testPlanApiScenarioExample.setOrderByClause("pos asc");
        List<TestPlanApiScenario> scenarioList = testPlanApiScenarioMapper.selectByExample(testPlanApiScenarioExample);

        //最后一个移动到第一位之前
        ResourceSortRequest request = new ResourceSortRequest();
        request.setTestCollectionId("wxxx_collection_2");
        request.setProjectId("wxx_project_1234");
        request.setMoveId(scenarioList.getLast().getId());
        request.setTargetId(scenarioList.getFirst().getId());
        request.setMoveMode(MoveTypeEnum.BEFORE.name());

        MvcResult result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_SORT, request);
        ResultHolder resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        TestPlanOperationResponse response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanOperationResponse.class);
        Assertions.assertEquals(response.getOperationCount(), 1);
        scenarioList = testPlanApiScenarioMapper.selectByExample(testPlanApiScenarioExample);
        Assertions.assertEquals(scenarioList.getFirst().getId(), request.getMoveId());
        Assertions.assertEquals(scenarioList.get(1).getId(), request.getTargetId());

        //将这时的第30个放到第一位之后
        request.setTargetId(scenarioList.getLast().getId());
        request.setMoveId(scenarioList.getFirst().getId());
        request.setMoveMode(MoveTypeEnum.AFTER.name());
        result = this.requestPostWithOkAndReturn(URL_POST_RESOURCE_API_SCENARIO_SORT, request);
        resultHolder = JSON.parseObject(result.getResponse().getContentAsString(StandardCharsets.UTF_8), ResultHolder.class);
        response = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestPlanOperationResponse.class);
        Assertions.assertEquals(response.getOperationCount(), 1);
        scenarioList = testPlanApiScenarioMapper.selectByExample(testPlanApiScenarioExample);
        Assertions.assertEquals(scenarioList.getFirst().getId(), request.getTargetId());
        Assertions.assertEquals(scenarioList.get(1).getId(), request.getMoveId());

    }

    @Test
    @Order(5)
    public void testApiScenarioModuleTree() throws Exception {
        TestPlanTreeRequest request = new TestPlanTreeRequest();
        request.setTestPlanId("wxxx_plan_1");
        request.setTreeType("MODULE");
        this.requestPostWithOkAndReturn(API_SCENARIO_TREE, request);
        request.setTestPlanId("wxxx_plan_2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_TREE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setTestPlanId("wxxx_plan_2");
        request.setTreeType("COLLECTION");
        MvcResult mvcResult1 = this.requestPostWithOkAndReturn(API_SCENARIO_TREE, request);
        String returnData1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder1 = JSON.parseObject(returnData1, ResultHolder.class);
        Assertions.assertNotNull(resultHolder1);
    }

    @Test
    @Order(6)
    public void testApiScenarioDisassociate() throws Exception {
        TestPlanDisassociationRequest request = new TestPlanDisassociationRequest();
        request.setTestPlanId("wxxx_plan_2");
        request.setId("wxxx_plan_scenario_3");

        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_DISASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }


    @Test
    @Order(7)
    public void testApiScenarioBatchDisassociate() throws Exception {
        BasePlanCaseBatchRequest request = new BasePlanCaseBatchRequest();
        request.setTestPlanId("wxxx_plan_2");
        request.setSelectAll(true);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_BATCH_DISASSOCIATE, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

    @Test
    @Order(5)
    public void testBatchUpdateExecutor() throws Exception {
        TestPlanApiScenarioUpdateRequest request = new TestPlanApiScenarioUpdateRequest();
        request.setUserId("test_user");
        request.setTestPlanId("wxxx_plan_2");
        request.setSelectAll(true);
        this.requestPostWithOk(API_SCENARIO_BATCH_UPDATE_EXECUTOR_URL, request);
        request.setTestPlanId("wxxx_plan_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("wxxx_plan_scenario_1"));
        this.requestPostWithOk(API_SCENARIO_BATCH_UPDATE_EXECUTOR_URL, request);
    }

    @Test
    @Order(6)
    public void testGet() throws Exception {
        List<ApiScenarioReport> reports = new ArrayList<>();
        ApiScenarioReport scenarioReport = new ApiScenarioReport();
        scenarioReport.setId("plan-test-scenario-report-id");
        scenarioReport.setProjectId(DEFAULT_PROJECT_ID);
        scenarioReport.setName("plan-test-scenario-report-name");
        scenarioReport.setStartTime(System.currentTimeMillis());
        scenarioReport.setCreateUser("admin");
        scenarioReport.setUpdateUser("admin");
        scenarioReport.setUpdateTime(System.currentTimeMillis());
        scenarioReport.setPoolId("11");
        scenarioReport.setEnvironmentId("env");
        scenarioReport.setTestPlanScenarioId("test-scenario-id");
        scenarioReport.setRunMode("api-run-mode");
        scenarioReport.setTriggerMode("api-trigger-mode");
        reports.add(scenarioReport);
        ApiScenarioRecord apiScenarioRecord = new ApiScenarioRecord();
        apiScenarioRecord.setApiScenarioId("test-scenario-record-id");
        apiScenarioRecord.setApiScenarioReportId(scenarioReport.getId());
        apiScenarioReportService.insertApiScenarioReport(reports, List.of(apiScenarioRecord));
        List<ApiScenarioReportStep> steps = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ApiScenarioReportStep apiScenarioReportStep = new ApiScenarioReportStep();
            apiScenarioReportStep.setStepId("plan-test-scenario-report-step-id" + i);
            apiScenarioReportStep.setReportId("plan-test-scenario-report-id");
            apiScenarioReportStep.setSort((long) i);
            if (i % 2 == 0) {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.API_CASE.name());
                apiScenarioReportStep.setParentId("NONE");
            } else if (i % 3 == 0) {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.IF_CONTROLLER.name());
                apiScenarioReportStep.setParentId("plan-test-scenario-report-step-id" + (i - 1));
            } else {
                apiScenarioReportStep.setStepType(ApiScenarioStepType.API_SCENARIO.name());
                apiScenarioReportStep.setParentId("plan-test-scenario-report-step-id" + (i - 2));
            }
            steps.add(apiScenarioReportStep);
        }
        apiScenarioReportService.insertApiScenarioReportStep(steps);
        List<ApiScenarioReportDetail> details = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            ApiScenarioReportDetail apiReportDetail = new ApiScenarioReportDetail();
            apiReportDetail.setId("plan-test-report-detail-id" + i + 2);
            apiReportDetail.setReportId("plan-test-scenario-report-id");
            apiReportDetail.setStepId("plan-test-scenario-report-step-id" + i);
            apiReportDetail.setSort((long) i);
            if (i % 2 == 0) {
                apiReportDetail.setStatus(ResultStatus.SUCCESS.name());
                apiReportDetail.setResponseSize(1L);
                apiReportDetail.setRequestTime(2L);
            } else if (i % 3 == 0) {
                apiReportDetail.setStatus(null);
                apiReportDetail.setResponseSize(0L);
                apiReportDetail.setRequestTime(2L);
            } else {
                apiReportDetail.setStatus(ResultStatus.FAKE_ERROR.name());
                apiReportDetail.setResponseSize(1L);
                apiReportDetail.setRequestTime(2L);
            }
            details.add(apiReportDetail);
        }
        apiScenarioReportDetailMapper.batchInsert(details);

        //插入console 资源池 环境
        ApiScenarioReportLog apiScenarioReportLog = new ApiScenarioReportLog();
        apiScenarioReportLog.setId(IDGenerator.nextStr());
        apiScenarioReportLog.setReportId("test-scenario-report-id");
        apiScenarioReportLog.setConsole("console".getBytes());
        apiScenarioReportLogMapper.insert(apiScenarioReportLog);


        MvcResult mvcResult = this.requestGetWithOk("/report/get/plan-test-scenario-report-id")
                .andReturn();
        ApiScenarioReportDTO apiReportDTO = ApiDataUtils.parseObject(JSON.toJSONString(parseResponse(mvcResult).get("data")), ApiScenarioReportDTO.class);

        Assertions.assertNotNull(apiReportDTO);
        Assertions.assertEquals(apiReportDTO.getId(), "plan-test-scenario-report-id");

        ApiScenarioReport scenarioReport1 = apiScenarioReportMapper.selectByPrimaryKey("plan-test-scenario-report-id");
        scenarioReport1.setTestPlanScenarioId("NONE");
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport1);
        mockMvc.perform(getRequestBuilder("/report/get/plan-test-scenario-report-id"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
        scenarioReport1.setTestPlanScenarioId("test-scenario-id");
        apiScenarioReportMapper.updateByPrimaryKeySelective(scenarioReport1);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.TEST_PLAN_REPORT_READ, "/report/get/plan-test-scenario-report-id");
        List<ApiScenarioReportDetail> reportsDetails = new ArrayList<>();
        List<ApiScenarioReportDetailBlob> reportBlogs = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            ApiScenarioReportDetail apiReportDetail = new ApiScenarioReportDetail();
            apiReportDetail.setId("plan-test-report-detail-id" + i);
            apiReportDetail.setReportId("plan-test-scenario-report-id");
            apiReportDetail.setStepId("plan-test-scenario-report-step-id1");
            apiReportDetail.setStatus("success");
            apiReportDetail.setResponseSize(0L);
            apiReportDetail.setRequestTime((long) i);
            apiReportDetail.setSort((long) i);
            reportsDetails.add(apiReportDetail);

            reportBlogs.add(new ApiScenarioReportDetailBlob() {{
                setId(apiReportDetail.getId());
                setReportId(apiReportDetail.getReportId());
                setContent("{\"resourceId\":\"\",\"stepId\":null,\"threadName\":\"Thread Group\",\"name\":\"HTTP Request1\",\"url\":\"https://www.baidu.com/\",\"requestSize\":195,\"startTime\":1705570589125,\"endTime\":1705570589310,\"error\":1,\"headers\":\"Connection: keep-alive\\nContent-Length: 0\\nContent-Type: application/x-www-form-urlencoded; charset=UTF-8\\nHost: www.baidu.com\\nUser-Agent: Apache-HttpClient/4.5.14 (Java/21)\\n\",\"cookies\":\"\",\"body\":\"POST https://www.baidu.com/\\n\\nPOST data:\\n\\n\\n[no cookies]\\n\",\"status\":\"ERROR\",\"method\":\"POST\",\"assertionTotal\":1,\"passAssertionsTotal\":0,\"subRequestResults\":[],\"responseResult\":{\"responseCode\":\"200\",\"responseMessage\":\"OK\",\"responseTime\":185,\"latency\":180,\"responseSize\":2559,\"headers\":\"HTTP/1.1 200 OK\\nContent-Length: 2443\\nContent-Type: text/html\\nServer: bfe\\nDate: Thu, 18 Jan 2024 09:36:29 GMT\\n\",\"body\":\"<!DOCTYPE html>\\r\\n<!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;charset=utf-8><meta http-equiv=X-UA-Compatible content=IE=Edge><meta content=always name=referrer><link rel=stylesheet type=text/css href=https://ss1.bdstatic.com/5eN1bjq8AAUYm2zgoY3K/r/www/cache/bdorz/baidu.min.css><title>百度一下，你就知道</title></head> <body link=#0000cc> <div id=wrapper> <div id=head> <div class=head_wrapper> <div class=s_form> <div class=s_form_wrapper> <div id=lg> <img hidefocus=true src=//www.baidu.com/img/bd_logo1.png width=270 height=129> </div> <form id=form name=f action=//www.baidu.com/s class=fm> <input type=hidden name=bdorz_come value=1> <input type=hidden name=ie value=utf-8> <input type=hidden name=f value=8> <input type=hidden name=rsv_bp value=1> <input type=hidden name=rsv_idx value=1> <input type=hidden name=tn value=baidu><span class=\\\"bg s_ipt_wr\\\"><input id=kw name=wd class=s_ipt value maxlength=255 autocomplete=off autofocus=autofocus></span><span class=\\\"bg s_btn_wr\\\"><input type=submit id=su value=百度一下 class=\\\"bg s_btn\\\" autofocus></span> </form> </div> </div> <div id=u1> <a href=http://news.baidu.com name=tj_trnews class=mnav>新闻</a> <a href=https://www.hao123.com name=tj_trhao123 class=mnav>hao123</a> <a href=http://map.baidu.com name=tj_trmap class=mnav>地图</a> <a href=http://v.baidu.com name=tj_trvideo class=mnav>视频</a> <a href=http://tieba.baidu.com name=tj_trtieba class=mnav>贴吧</a> <noscript> <a href=http://www.baidu.com/bdorz/login.gif?login&amp;tpl=mn&amp;u=http%3A%2F%2Fwww.baidu.com%2f%3fbdorz_come%3d1 name=tj_login class=lb>登录</a> </noscript> <script>document.write('<a href=\\\"http://www.baidu.com/bdorz/login.gif?login&tpl=mn&u='+ encodeURIComponent(window.location.href+ (window.location.search === \\\"\\\" ? \\\"?\\\" : \\\"&\\\")+ \\\"bdorz_come=1\\\")+ '\\\" name=\\\"tj_login\\\" class=\\\"lb\\\">登录</a>');\\r\\n                </script> <a href=//www.baidu.com/more/ name=tj_briicon class=bri style=\\\"display: block;\\\">更多产品</a> </div> </div> </div> <div id=ftCon> <div id=ftConw> <p id=lh> <a href=http://home.baidu.com>关于百度</a> <a href=http://ir.baidu.com>About Baidu</a> </p> <p id=cp>&copy;2017&nbsp;Baidu&nbsp;<a href=http://www.baidu.com/duty/>使用百度前必读</a>&nbsp; <a href=http://jianyi.baidu.com/ class=cp-feedback>意见反馈</a>&nbsp;京ICP证030173号&nbsp; <img src=//www.baidu.com/img/gs.gif> </p> </div> </div> </div> </body> </html>\\r\\n\",\"contentType\":\"text/html\",\"vars\":null,\"imageUrl\":null,\"socketInitTime\":14,\"dnsLookupTime\":0,\"tcpHandshakeTime\":0,\"sslHandshakeTime\":0,\"transferStartTime\":166,\"downloadTime\":5,\"bodySize\":2443,\"headerSize\":116,\"assertions\":[{\"name\":\"JSON Assertion\",\"content\":null,\"script\":null,\"message\":\"Expected to find an object with property ['test'] in path $ but found 'java.lang.String'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.\",\"pass\":false}]},\"isSuccessful\":false,\"fakeErrorMessage\":\"\",\"fakeErrorCode\":null}\n".getBytes());

            }});
        }
        apiScenarioReportDetailMapper.batchInsert(reportsDetails);
        apiScenarioReportDetailBlobMapper.batchInsert(reportBlogs);

        this.requestGetWithOk("/report/get/detail/plan-test-scenario-report-id/plan-test-scenario-report-step-id1")
                .andReturn();
        requestGetPermissionTest(PermissionConstants.TEST_PLAN_REPORT_READ, "/report/get/detail/plan-test-scenario-report-id/plan-test-scenario-report-step-id1");
    }

    @Test
    @Order(7)
    void testAssociateBugPage() throws Exception {
        BugPageProviderRequest request = new BugPageProviderRequest();
        request.setSourceId("test_plan_case_id");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(BUG_ASSOCIATE_PAGE, request);
    }

    @Test
    @Order(8)
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
    @Order(9)
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
        this.requestMultipartWithOkAndReturn(API_SCENARIO_BATCH_ADD_BUG_URL, paramMap);

        request.setSelectAll(true);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        paramMap.add("files", files);
        this.requestMultipartWithOkAndReturn(API_SCENARIO_BATCH_ADD_BUG_URL, paramMap);
    }

    private TestPlanCaseBatchAddBugRequest buildRequest(boolean isUpdate) {
        TestPlanCaseBatchAddBugRequest request = new TestPlanCaseBatchAddBugRequest();
        request.setTestPlanId("wxxx_plan_1");
        request.setProjectId("wxx_project_1234");
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
        request.setTestPlanId("wxxx_plan_1");
        this.requestPostWithOk(API_SCENARIO_BATCH_ASSOCIATE_BUG_URL, request);
        request.setSelectAll(true);
        this.requestPostWithOk(API_SCENARIO_BATCH_ASSOCIATE_BUG_URL, request);
    }
}
