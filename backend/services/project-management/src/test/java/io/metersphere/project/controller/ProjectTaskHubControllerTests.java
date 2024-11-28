package io.metersphere.project.controller;

import io.metersphere.sdk.constants.ExecTaskType;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.ExecTaskItem;
import io.metersphere.system.domain.ExecTaskItemExample;
import io.metersphere.system.dto.request.BatchExecTaskPageRequest;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.dto.taskhub.request.ScheduleRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemBatchRequest;
import io.metersphere.system.dto.taskhub.request.TaskHubItemRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.ExecTaskItemMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTaskHubControllerTests extends BaseTest {

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;
    @Resource
    private ExecTaskItemMapper execTaskItemMapper;

    /**
     * 项目任务中心测试用例
     */
    public static final String PROJECT_TASK_PAGE = "/project/task-center/exec-task/page";
    public static final String PROJECT_SCHEDULE_TASK_PAGE = "/project/task-center/schedule/page";
    public static final String PROJECT_TASK_ITEM_PAGE = "/project/task-center/exec-task/item/page";
    public static final String PROJECT_STATISTICS = "/project/task-center/exec-task/statistics";
    public static final String PROJECT_RESOURCE_POOL_OPTIONS = "/project/task-center/resource-pool/options";
    public static final String PROJECT_TASK_STOP = "/project/task-center/exec-task/stop/";
    public static final String PROJECT_TASK_RERUN = "/project/task-center/exec-task/rerun/{0}";
    public static final String PROJECT_TASK_DELETE = "/project/task-center/exec-task/delete/";
    public static final String PROJECT_TASK_BATCH_STOP = "/project/task-center/exec-task/batch-stop";
    public static final String PROJECT_TASK_ITEM_ORDER = "/project/task-center/exec-task/item/order";
    public static final String PROJECT_TASK_BATCH_DELETE = "/organization/task-center/exec-task/batch-delete";
    public static final String PROJECT_TASK_ITEM_STOP = "/project/task-center/exec-task/item/stop/";
    public static final String PROJECT_TASK_ITEM_BATCH_STOP = "/project/task-center/exec-task/item/batch-stop";
    public static final String PROJECT_SCHEDULE_TASK_DELETE = "/project/task-center/schedule/delete/";
    public static final String PROJECT_SCHEDULE_TASK_SWITCH = "/project/task-center/schedule/switch/";
    public static final String PROJECT_SCHEDULE_TASK_BATCH_ENABLE = "/project/task-center/schedule/batch-enable";
    public static final String PROJECT_SCHEDULE_TASK_BATCH_DISABLE = "/project/task-center/schedule/batch-disable";
    public static final String PROJECT_SCHEDULE_TASK_UPDATE_CRON = "/project/task-center/schedule/update-cron";
	public static final String PROJECT_BATCH_TASK_PAGE = "/project/task-center/exec-task/batch/page";

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
    @Order(5)
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
        mockPost("/api/task/item/stop", "");
        this.requestGet(PROJECT_TASK_STOP + "pro_1");
        mockPost("/api/task/stop", "");
        this.requestGet(PROJECT_TASK_STOP + "pro_2");
    }

    /**
     * 项目执行任务停止
     */
    @Test
    @Order(6)
    public void projectTaskRerun() throws Exception {
        String taskId = "pro_1";
        this.requestGet(PROJECT_TASK_RERUN, taskId);
        // @@校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_CASE_TASK_CENTER_EXEC_STOP, PROJECT_TASK_RERUN, taskId);
        // @@校验日志
        checkLog(taskId, OperationLogType.RERUN);
    }

    /**
     * 系统执行任务停止
     */
    @Test
    @Order(23)
    public void projectTaskBatchStop() throws Exception {
        mockPost("/api/task/item/stop", "");
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectAll(true);
        this.requestPost(PROJECT_TASK_BATCH_STOP, request);
        request.setSelectAll(false);
        request.setSelectIds(List.of("pro_1", "pro_2"));
        this.requestPost(PROJECT_TASK_BATCH_STOP, request);
    }

    /**
     * 获取任务项的排队信息
     */
    @Test
    @Order(23)
    public void projectGetTaskItemOrder() throws Exception {
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PROJECT_TASK_ITEM_ORDER, List.of("1"));
        HashMap resultData = getResultData(mvcResult, HashMap.class);
        // 返回请求正常
        Assertions.assertNotNull(resultData);
    }

    /**
     * 项目执行任务删除
     */
    @Test
    @Order(7)
    public void projectTaskDelete() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_TASK_DELETE + "4");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(8)
    public void projectBatchTaskDelete() throws Exception {
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectAll(false);
        request.setSelectIds(List.of("1"));
        this.requestPost(PROJECT_TASK_BATCH_DELETE, request);
    }


    /**
     * 项目执行任务项停止
     */
    @Test
    @Order(4)
    public void projectTaskItemStop() throws Exception {
        ExecTaskItem execTaskItem = new ExecTaskItem();
        execTaskItem.setResourcePoolNode(host + ":" + port);
        ExecTaskItemExample itemExample = new ExecTaskItemExample();
        itemExample.createCriteria().andIdIn(List.of("pro_1", "pro_2"));
        execTaskItemMapper.updateByExampleSelective(execTaskItem, itemExample);
        mockPost("/api/task/item/stop", "");
        this.requestGet(PROJECT_TASK_ITEM_STOP + "pro_1");
    }


    @Test
    @Order(5)
    public void projectTaskItemBatchStop() throws Exception {
        mockPost("/api/task/item/stop", "");
        TaskHubItemBatchRequest request = new TaskHubItemBatchRequest();
        request.setSelectAll(false);
        request.setSelectIds(List.of("pro_1", "pro_2"));
        this.requestPostWithOkAndReturn(PROJECT_TASK_ITEM_BATCH_STOP, request);
    }

    /**
     * 项目后台任务删除
     */
    @Test
    @Order(5)
    public void projectScheduleTaskDelete() throws Exception {
        this.requestGet(PROJECT_SCHEDULE_TASK_DELETE + "123143");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PROJECT_SCHEDULE_TASK_DELETE + "pro_wx_1");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    @Test
    @Order(6)
    public void projectScheduleTaskSwitch() throws Exception {
        this.requestGet(PROJECT_SCHEDULE_TASK_SWITCH + "pro_wx_1");
    }

    /**
     * 系统后台任务删除
     */
    @Test
    @Order(7)
    public void projectScheduleBatchEnable() throws Exception {
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectAll(true);
        this.requestPost(PROJECT_SCHEDULE_TASK_BATCH_ENABLE, request);
        request.setSelectAll(false);
        request.setSelectIds(List.of("pro_wx_1", "pro_wx_1"));
        this.requestPost(PROJECT_SCHEDULE_TASK_BATCH_ENABLE, request);
    }

    @Test
    @Order(8)
    public void projectScheduleBatchDisable() throws Exception {
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectAll(true);
        this.requestPost(PROJECT_SCHEDULE_TASK_BATCH_DISABLE, request);
    }

    @Test
    @Order(9)
    public void projectScheduleUpdateCron() throws Exception {
        ScheduleRequest request = new ScheduleRequest();
        request.setCron("0 0 0 1 * ?");
        request.setId("pro_wx_1");
        this.requestPost(PROJECT_SCHEDULE_TASK_UPDATE_CRON, request);
        request.setId("pro_wx_2");
        this.requestPost(PROJECT_SCHEDULE_TASK_UPDATE_CRON, request);
    }

    @Test
    @Order(10)
    public void getProjectBatchTaskPage() throws Exception {
        BatchExecTaskPageRequest request = new BatchExecTaskPageRequest();
        request.setBatchType(ExecTaskType.API_CASE_BATCH.name());
        request.setTaskId("pro_4");
        request.setCurrent(1);
        request.setPageSize(10);
        this.requestPostWithOk(PROJECT_BATCH_TASK_PAGE, request);
        request.setSort(Map.of("createTime", "asc"));
        request.setBatchType(ExecTaskType.API_SCENARIO_BATCH.name());
        this.requestPostWithOk(PROJECT_BATCH_TASK_PAGE, request);
    }
}
