package io.metersphere.project.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BasePageRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

public class ProjectTaskHubControllerTests extends BaseTest {
    /**
     * 项目任务中心测试用例
     */
    public static final String PROJECT_TASK_PAGE = "/project/task-center/exec-task/page";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_exec_task_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void getProjectTaskPage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(PROJECT_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PROJECT_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }
}
