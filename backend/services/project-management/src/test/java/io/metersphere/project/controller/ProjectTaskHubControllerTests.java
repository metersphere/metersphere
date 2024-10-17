package io.metersphere.project.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProjectTaskHubControllerTests extends BaseTest {
    /**
     * 项目任务中心测试用例
     */
    public static final String PROJECT_TASK_PAGE = "/project/task-center/exec-task/page";
    public static final String PROJECT_SCHEDULE_TASK_PAGE = "/project/task-center/schedule/page";
    public static final String PROJECT_TASK_ITEM_PAGE = "/project/task-center/exec-task/item/page";
    public static final String PROJECT_STATISTICS = "/project/task-center/exec-task/statistics";
    public static final String PROJECT_RESOURCE_POOL_OPTIONS = "/project/task-center/resource-pool/options";
    public static final String PROJECT_TASK_STOP = "/project/task-center/exec-task/stop/";
    public static final String PROJECT_TASK_DELETE = "/project/task-center/exec-task/delete/";

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


    /**
     * 项目后台任务
     */
    @Test
    @Order(2)
    public void getProjectSchedulePage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(PROJECT_SCHEDULE_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PROJECT_SCHEDULE_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    /**
     * 执行任务详情列表
     */
    @Test
    @Order(3)
    public void getProjectTaskItemPage() throws Exception {
        TaskHubItemRequest request = new TaskHubItemRequest();
        request.setTaskId("pro_1");
        this.requestPost(PROJECT_TASK_ITEM_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PROJECT_TASK_ITEM_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(4)
    public void getProStatistics() throws Exception {
        List<String> ids = List.of("pro_1", "pro_2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PROJECT_STATISTICS, ids);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * 组织获取资源池下拉选项
     */
    @Test
    @Order(5)
    public void getOrgResourcePoolOptions() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_RESOURCE_POOL_OPTIONS);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * 项目执行任务停止
     */
    @Test
    @Order(6)
    public void projectTaskStop() throws Exception {
        this.requestGet(PROJECT_TASK_STOP + "pro_1");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_TASK_STOP + "pro_2");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    /**
     * 项目执行任务删除
     */
    @Test
    @Order(5)
    public void projectTaskDelete() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_TASK_DELETE + "4");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }
}
