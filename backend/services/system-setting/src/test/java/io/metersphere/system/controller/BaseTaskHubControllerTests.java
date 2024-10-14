package io.metersphere.system.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.ExecTask;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.service.BaseTaskHubService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseTaskHubControllerTests extends BaseTest {

    @Resource
    private BaseTaskHubService baseTaskHubService;

    /**
     * 系统任务中心测试用例
     */
    public static final String SYSTEM_TASK_PAGE = "/system/task-center/exec-task/page";
    public static final String SYSTEM_SCHEDULE_TASK_PAGE = "/system/task-center/schedule/page";
    public static final String SYSTEM_TASK_ITEM_PAGE = "/system/task-center/exec-task/item/page";
    public static final String SYSTEM_STATISTICS = "/system/task-center/exec-task/statistics";
    public static final String SYSTEM_RESOURCE_POOL_OPTIONS = "/system/task-center/resource-pool/options";
    public static final String SYSTEM_RESOURCE_POOL_STATUS = "/system/task-center/resource-pool/status";

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
     * 系统后台任务
     */
    @Test
    @Order(2)
    public void getSystemSchedulePage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(SYSTEM_SCHEDULE_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SYSTEM_SCHEDULE_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    /**
     * 系统任务详情
     */
    @Test
    @Order(3)
    public void getSystemTaskItemPage() throws Exception {
        TaskHubItemRequest request = new TaskHubItemRequest();
        request.setTaskId("1");
        this.requestPost(SYSTEM_TASK_ITEM_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SYSTEM_TASK_ITEM_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * 系统用例任务完成率统计
     */
    @Test
    @Order(3)
    public void getStatistics() throws Exception {
        List<String> ids = List.of("1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SYSTEM_STATISTICS, ids);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * 系统获取资源池下拉选项
     */
    @Test
    @Order(3)
    public void getSystemResourcePoolOptions() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(SYSTEM_RESOURCE_POOL_OPTIONS);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * 校验资源池节点状态
     */
    @Test
    @Order(3)
    public void getResourcePoolStatus() throws Exception {
        List<String> ids = List.of("1");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(SYSTEM_RESOURCE_POOL_STATUS, ids);
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
    public static final String ORG_SCHEDULE_TASK_PAGE = "/organization/task-center/schedule/page";
    public static final String ORG_TASK_ITEM_PAGE = "/organization/task-center/exec-task/item/page";
    public static final String ORG_STATISTICS = "/organization/task-center/exec-task/statistics";
    public static final String ORG_RESOURCE_POOL_OPTIONS = "/organization/task-center/resource-pool/options";

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


    /**
     * 组织后台任务
     */
    @Test
    @Order(21)
    public void getOrgSchedulePage() throws Exception {
        BasePageRequest request = new BasePageRequest();
        this.requestPost(ORG_SCHEDULE_TASK_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ORG_SCHEDULE_TASK_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    /**
     * 组织任务详情
     */
    @Test
    @Order(3)
    public void getOrgTaskItemPage() throws Exception {
        TaskHubItemRequest request = new TaskHubItemRequest();
        request.setTaskId("1");
        this.requestPost(ORG_TASK_ITEM_PAGE, request);
        request.setCurrent(1);
        request.setPageSize(10);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ORG_TASK_ITEM_PAGE, request);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(4)
    public void getOrgStatistics() throws Exception {
        List<String> ids = List.of("1", "2");
        MvcResult mvcResult = this.requestPostWithOkAndReturn(ORG_STATISTICS, ids);
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
        MvcResult mvcResult = this.requestGetWithOkAndReturn(ORG_RESOURCE_POOL_OPTIONS);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    @Test
    @Order(21)
    public void testInsert() throws Exception {
        baseTaskHubService.insertExecTaskAndDetail(new ArrayList<>());
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setId("1111");
        execTaskItem.setTaskId("1");
        execTaskItem.setResourceId("1");
        execTaskItem.setStatus("SUCCESS");
        execTaskItem.setResourcePoolId("1");
        execTaskItem.setResourceType("FUNCTIONAL");
        execTaskItem.setProjectId("1234");
        execTaskItem.setOrganizationId("1234123");
        execTaskItem.setExecutor("admin");
        execTaskItem.setResourceName("测试");
        baseTaskHubService.insertExecTaskAndDetail(List.of(execTaskItem));


        baseTaskHubService.insertExecTaskAndDetail(new ArrayList<>(), new ArrayList<>());
        execTaskItem.setId("2333");
        ExecTask execTask = new ExecTask();
        execTask.setId("121321");
        execTask.setNum(123L);
        execTask.setTaskName("名称");
        execTask.setStatus("SUCCESS");
        execTask.setCaseCount(123L);
        execTask.setTaskType("API_CASE");
        execTask.setTriggerMode("API");
        execTask.setProjectId("1234");
        execTask.setOrganizationId("123432");
        execTask.setCreateTime(System.currentTimeMillis());
        execTask.setCreateUser("admin");
        baseTaskHubService.insertExecTaskAndDetail(List.of(execTask), List.of(execTaskItem));

    }
}
