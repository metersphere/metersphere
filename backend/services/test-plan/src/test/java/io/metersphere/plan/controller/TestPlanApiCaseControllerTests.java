package io.metersphere.plan.controller;

import io.metersphere.plan.constants.AssociateCaseType;
import io.metersphere.plan.dto.request.*;
import io.metersphere.plan.service.TestPlanApiCaseService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanApiCaseControllerTests extends BaseTest {

    public static final String API_CASE_PAGE = "/test-plan/api/case/page";
    public static final String API_CASE_TREE_COUNT = "/test-plan/api/case/module/count";
    public static final String API_CASE_TREE_MODULE_TREE = "/test-plan/api/case/tree";
    public static final String API_CASE_DISASSOCIATE = "/test-plan/api/case/disassociate";
    public static final String API_CASE_BATCH_DISASSOCIATE = "/test-plan/api/case/batch/disassociate";
    public static final String API_CASE_BATCH_UPDATE_EXECUTOR_URL = "/test-plan/api/case/batch/update/executor";

    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;

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
    public void testApiCaseAssociate() throws Exception {
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

    }
}
