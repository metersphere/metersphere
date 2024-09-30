package io.metersphere.system.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BasePageRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseTaskHubControllerTests extends BaseTest {

    /**
     * 系统任务中心测试用例
     */
    public static final String SYSTEM_TASK_PAGE = "/system/task-center/exec-task/page";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_exec_task_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getSystemTaskPage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(SYSTEM_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SYSTEM_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    /**
     * 组织任务中心测试用例
     */
    public static final String ORG_TASK_PAGE = "/organization/task-center/exec-task/page";

    @Test
    @Order(20)
    public void getOrgTaskPage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(ORG_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ORG_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }
}
