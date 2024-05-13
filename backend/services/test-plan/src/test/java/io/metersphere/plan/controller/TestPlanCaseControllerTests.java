package io.metersphere.plan.controller;

import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.request.BasePlanCaseBatchRequest;
import io.metersphere.plan.dto.request.TestPlanCaseRequest;
import io.metersphere.plan.dto.request.TestPlanDisassociationRequest;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
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
import java.util.HashMap;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanCaseControllerTests extends BaseTest {

    public static final String FUNCTIONAL_CASE_LIST_URL = "/test-plan/functional/case/page";
    public static final String FUNCTIONAL_CASE_TREE_URL = "/test-plan/functional/case/tree/";
    public static final String FUNCTIONAL_CASE_TREE_COUNT_URL = "/test-plan/functional/case/module/count";
    public static final String FUNCTIONAL_CASE_DISASSOCIATE_URL = "/test-plan/functional/case/disassociate";
    public static final String FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL = "/test-plan/functional/case/batch/disassociate";


    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_case_relate_bug.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetFunctionalCaseList() throws Exception {
        TestPlanCaseRequest request = new TestPlanCaseRequest();
        request.setProjectId("123");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("plan_1");
        this.requestPost(FUNCTIONAL_CASE_LIST_URL, request);
        request.setSort(new HashMap<>() {{
            put("createTime", "desc");
        }});
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_LIST_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(2)
    public void testGetFunctionalCaseTree() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_TREE_URL + "plan_1");
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);

        this.requestGetWithOkAndReturn(FUNCTIONAL_CASE_TREE_URL + "plan_2");
    }

    @Test
    @Order(3)
    public void testGetFunctionalCaseTreeCount() throws Exception {
        TestPlanCaseRequest request = new TestPlanCaseRequest();
        request.setProjectId("123");
        request.setCurrent(1);
        request.setPageSize(10);
        request.setTestPlanId("plan_1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(FUNCTIONAL_CASE_TREE_COUNT_URL, request);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(4)
    void disassociate() throws Exception {
        TestPlanDisassociationRequest request = new TestPlanDisassociationRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setRefId("gyq_disassociate_case_3");
        this.requestPostWithOk(FUNCTIONAL_CASE_DISASSOCIATE_URL, request);
    }

    @Test
    @Order(5)
    public void disassociateBatch() throws Exception {
        BasePlanCaseBatchRequest request = new BasePlanCaseBatchRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setSelectAll(true);
        request.setExcludeIds(List.of("gyq_disassociate_case_2"));
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL, request);
        TestPlanFunctionalCaseExample testPlanFunctionalCaseExample = new TestPlanFunctionalCaseExample();
        testPlanFunctionalCaseExample.createCriteria().andTestPlanIdEqualTo("gyq_disassociate_plan_1");
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        Assertions.assertEquals(1,testPlanFunctionalCases.size());
        request = new BasePlanCaseBatchRequest();
        request.setTestPlanId("gyq_disassociate_plan_1");
        request.setSelectAll(false);
        request.setSelectIds(List.of("gyq_disassociate_case_2"));
        this.requestPostWithOk(FUNCTIONAL_CASE_BATCH_DISASSOCIATE_URL, request);
        testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(testPlanFunctionalCaseExample);
        Assertions.assertEquals(0,testPlanFunctionalCases.size());
    }
}
