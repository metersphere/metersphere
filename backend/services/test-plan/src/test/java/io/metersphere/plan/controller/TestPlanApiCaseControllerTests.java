package io.metersphere.plan.controller;

import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.request.TestPlanDisassociationRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPlanApiCaseControllerTests extends BaseTest {

    public static final String API_CASE_PAGE = "/test-plan/api/case/page";
    public static final String API_CASE_TREE_COUNT = "/test-plan/api/case/module/count";
    public static final String API_CASE_TREE_MODULE_TREE = "/test-plan/api/case/tree/";
    public static final String API_CASE_DISASSOCIATE = "/test-plan/api/case/disassociate";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_api_case.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testApiCasePageList() throws Exception {
        TestPlanApiCaseRequest request = new TestPlanApiCaseRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("wxxx_1");
        request.setProjectId("wxx_1234");
        request.setProtocol("HTTP");
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
        TestPlanApiCaseRequest request = new TestPlanApiCaseRequest();
        request.setTestPlanId("wxxx_1");
        request.setProjectId("wxx_1234");
        request.setProtocol("HTTP");
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_TREE_COUNT, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(3)
    public void testApiCaseModuleTree() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(API_CASE_TREE_MODULE_TREE + "wxxx_1");
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        this.requestGetWithOkAndReturn(API_CASE_TREE_MODULE_TREE + "wxxx_2");
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
}
