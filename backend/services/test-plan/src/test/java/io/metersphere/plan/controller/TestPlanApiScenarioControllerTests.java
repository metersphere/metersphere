package io.metersphere.plan.controller;

import io.metersphere.api.constants.ApiScenarioStatus;
import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.dto.scenario.ApiScenarioAddRequest;
import io.metersphere.api.dto.scenario.ApiScenarioStepRequest;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.api.dto.scenario.ScenarioOtherConfig;
import io.metersphere.api.service.scenario.ApiScenarioService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plan.domain.TestPlanApiScenario;
import io.metersphere.plan.dto.request.TestPlanApiScenarioModuleRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioTreeRequest;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import io.metersphere.plan.service.TestPlanApiScenarioService;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.assertion.MsScriptAssertion;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

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

    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    private static ApiScenario apiScenario;
    private static TestPlanApiScenario testPlanApiScenario;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;

    @Override
    public String getBasePath() {
        return BASE_PATH;
    }

    @Test
    @Order(1)
    public void associate() {
        apiScenario = initApiData();
        TestPlanApiScenario testPlanApiScenario = new TestPlanApiScenario();
        testPlanApiScenario.setApiScenarioId(apiScenario.getId());
        testPlanApiScenario.setTestPlanId("wxxx_1");
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
        assertErrorCode(this.requestGet(RUN, testPlanApiScenario.getId()), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);
        assertErrorCode(this.requestGet(RUN_WITH_REPORT_ID, testPlanApiScenario.getId(), "reportId"), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);
        assertErrorCode(this.requestGet(RUN, "11"), NOT_FOUND);
        GetRunScriptRequest request = new GetRunScriptRequest();
        TaskItem taskItem = new TaskItem();
        taskItem.setResourceId(testPlanApiScenario.getId());
        taskItem.setReportId("reportId");
        request.setTaskItem(taskItem);
        testPlanApiScenarioService.getRunScript(request);
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

    }

    @Test
    @Order(5)
    public void testApiScenarioModuleTree() throws Exception {
        TestPlanApiScenarioTreeRequest request = new TestPlanApiScenarioTreeRequest();
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
}
