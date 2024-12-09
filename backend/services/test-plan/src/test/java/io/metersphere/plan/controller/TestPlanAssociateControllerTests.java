package io.metersphere.plan.controller;

import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.functional.request.FunctionalCasePageRequest;
import io.metersphere.plan.dto.request.TestPlanApiCaseRequest;
import io.metersphere.plan.dto.request.TestPlanApiRequest;
import io.metersphere.plan.dto.request.TestPlanApiScenarioRequest;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanAssociateControllerTests extends BaseTest {
    public static final String FUNCTIONAL_CASE_ASSOCIATION_URL = "/test-plan/association/page";
    public static final String API_ASSOCIATION_URL = "/test-plan/association/api/page";
    public static final String API_CASE_ASSOCIATION_URL = "/test-plan/association/api/case/page";
    public static final String API_SCENARIO_URL = "/test-plan/association/api/scenario/page";
    public static final String API_CASE_MODULE_COUNT_URL = "/test-plan/association/api/case/module/count";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_association.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetPageList() throws Exception {
        FunctionalCasePageRequest request = new FunctionalCasePageRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("1234567");
        this.requestPost(FUNCTIONAL_CASE_ASSOCIATION_URL, request);
        request.setProjectId("wx_1234");
        this.requestPost(FUNCTIONAL_CASE_ASSOCIATION_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        request.setTestPlanId("wxx_1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_ASSOCIATION_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);


        request.setCombineSearch(
                new CombineSearch() {{
                    this.setConditions(Collections.singletonList(new CombineCondition() {{
                        this.setName("belongTestPlan");
                        this.setOperator(CombineConditionOperator.EQUALS.name());
                        this.setValue("testABCD");
                    }}));
                }}
        );
        mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_ASSOCIATION_URL, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(2)
    public void testApiPageList() throws Exception {
        TestPlanApiRequest request = new TestPlanApiRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("wxx_1");
        request.setProjectId("1234567");
        this.requestPost(API_ASSOCIATION_URL, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPost(API_ASSOCIATION_URL, request);
        request.setProjectId("wx_1234");
        this.requestPost(API_ASSOCIATION_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_ASSOCIATION_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setCombineSearch(
                new CombineSearch() {{
                    this.setConditions(Collections.singletonList(new CombineCondition() {{
                        this.setName("belongTestPlan");
                        this.setOperator(CombineConditionOperator.EQUALS.name());
                        this.setValue("testABCD");
                    }}));
                }}
        );
        mvcResult = this.requestPostWithOkAndReturn(API_ASSOCIATION_URL, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(3)
    public void testApiCasePageList() throws Exception {
        TestPlanApiCaseRequest request = new TestPlanApiCaseRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setProjectId("1234567");
        request.setTestPlanId("wxx_1");
        this.requestPost(API_CASE_ASSOCIATION_URL, request);
        request.setProtocols(List.of("HTTP"));
        this.requestPost(API_CASE_ASSOCIATION_URL, request);
        request.setProjectId("wx_1234");
        this.requestPost(API_CASE_ASSOCIATION_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_ASSOCIATION_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);


        request.setCombineSearch(
                new CombineSearch() {{
                    this.setConditions(Collections.singletonList(new CombineCondition() {{
                        this.setName("belongTestPlan");
                        this.setOperator(CombineConditionOperator.EQUALS.name());
                        this.setValue("testABCD");
                    }}));
                }}
        );
        mvcResult = this.requestPostWithOkAndReturn(API_CASE_ASSOCIATION_URL, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(4)
    public void testApiScenarioPageList() throws Exception {
        TestPlanApiScenarioRequest request = new TestPlanApiScenarioRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("wxx_1");
        request.setProjectId("1234567");
        this.requestPost(API_SCENARIO_URL, request);
        request.setProjectId("wx_1234");
        this.requestPost(API_SCENARIO_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        request.setCombineSearch(
                new CombineSearch() {{
                    this.setConditions(Collections.singletonList(new CombineCondition() {{
                        this.setName("belongTestPlan");
                        this.setOperator(CombineConditionOperator.EQUALS.name());
                        this.setValue("testABCD");
                    }}));
                }}
        );
        mvcResult = this.requestPostWithOkAndReturn(API_SCENARIO_URL, request);
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(5)
    public void testApiCaseModuleCount() throws Exception {
        ApiModuleRequest request = new ApiModuleRequest();
        request.setProjectId("1234567");
        request.setTestPlanId("wxx_1");
        this.requestPostWithOkAndReturn(API_CASE_MODULE_COUNT_URL, request);
        request.setProtocols(List.of("HTTP"));
        request.setProjectId("wx_1234");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(API_CASE_MODULE_COUNT_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

    }

}
