package io.metersphere.plan.controller;

import io.metersphere.plan.dto.TestPlanCollectionMinderTreeDTO;
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
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class TestPlanCollectionMinderControllerTests extends BaseTest {

    private static final String PLAN_MIND = "/test-plan/mind/data/";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_test_plan_mind.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void tesPagePlanReportSuccess() throws Exception {

        MvcResult mvcResult = this.requestGetWithOkAndReturn(PLAN_MIND+"gyq_plan_1");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        List<TestPlanCollectionMinderTreeDTO> testPlanCollectionMinderTreeDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), TestPlanCollectionMinderTreeDTO.class);
        // 返回值不为空
        Assertions.assertNotNull(testPlanCollectionMinderTreeDTOS);
    }


}
