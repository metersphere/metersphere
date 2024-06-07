package io.metersphere.plan.controller;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.controller.result.ApiResultCode;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiDefinitionAddRequest;
import io.metersphere.api.dto.definition.ApiTestCaseAddRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.api.dto.request.http.body.RawBody;
import io.metersphere.api.service.definition.ApiDefinitionService;
import io.metersphere.api.service.definition.ApiTestCaseService;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.domain.TestPlanApiCase;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import io.metersphere.plan.service.TestPlanApiCaseService;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.api.task.GetRunScriptRequest;
import io.metersphere.sdk.dto.api.task.TaskItem;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;

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
    public static final String RUN = "run/{0}";
    public static final String RUN_WITH_REPORT_ID = "run/{0}?reportId={1}";

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;

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
        baseCollectionAssociateRequest.setCollectionId("wxxx_1");
        baseCollectionAssociateRequest.setIds(List.of("wxxx_api_1"));
        baseCollectionAssociateRequests.add(baseCollectionAssociateRequest);
        collectionAssociates.put(AssociateCaseType.API, baseCollectionAssociateRequests);
        testPlanApiCaseService.associateCollection("wxxx_2", collectionAssociates, "wx");

        //api case
        Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates1 = new HashMap<>();
        List<BaseCollectionAssociateRequest> baseCollectionAssociateRequests1 = new ArrayList<>();
        BaseCollectionAssociateRequest baseCollectionAssociateRequest1 = new BaseCollectionAssociateRequest();
        baseCollectionAssociateRequest1.setCollectionId("wxxx_1");
        baseCollectionAssociateRequest1.setIds(List.of("wxxx_api_case_1"));
        baseCollectionAssociateRequests1.add(baseCollectionAssociateRequest1);
        collectionAssociates1.put(AssociateCaseType.API_CASE, baseCollectionAssociateRequests1);
        testPlanApiCaseService.associateCollection("wxxx_2", collectionAssociates1, "wx");

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

    @Test
    @Order(7)
    public void run() throws Exception {
        assertErrorCode(this.requestGet(RUN, testPlanApiCase.getId()), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);
        assertErrorCode(this.requestGet(RUN_WITH_REPORT_ID, testPlanApiCase.getId(), "reportId"), ApiResultCode.RESOURCE_POOL_EXECUTE_ERROR);
        assertErrorCode(this.requestGet(RUN, "11"), NOT_FOUND);
        GetRunScriptRequest request = new GetRunScriptRequest();
        TaskItem taskItem = new TaskItem();
        taskItem.setResourceId(testPlanApiCase.getId());
        taskItem.setReportId("reportId");
        request.setTaskItem(taskItem);
        testPlanApiCaseService.getRunScript(request);

        requestGetPermissionTest(PermissionConstants.TEST_PLAN_READ_EXECUTE, RUN, testPlanApiCase.getId());
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
}
