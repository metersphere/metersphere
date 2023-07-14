package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.controller.param.OperationLogRequestDefinition;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperationLogControllerTests extends BaseTest {

    public static final String OPERATION_LOG_LIST = "/operating/log/list";

    public static final String OPTIONS_LIST = "/operating/log/get/options";


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_operation_log_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testOperationLogList() throws Exception {
        OperationLogRequest request = buildParam();
        MvcResult mvcResult = this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);
        Pager<List<OperationLogResponse>> pageResult = getPageResult(mvcResult, OperationLogResponse.class);
        List<OperationLogResponse> listRes = pageResult.getList();

        // @@异常参数校验
        updatedGroupParamValidateTest(OperationLogRequestDefinition.class, OPERATION_LOG_LIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPERATION_LOG_LIST, request);

    }

    @Test
    @Order(2)
    public void testGetOptions() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(OPTIONS_LIST);

        // @@校验权限
        requestGetPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPTIONS_LIST);
    }

    //TODO 异常用例补充


    private OperationLogRequest buildParam() {
        OperationLogRequest request = new OperationLogRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setStartTime(1689131059000l);
        request.setEndTime(1689149059000l);
        request.setLevel("system");
        return request;
    }


}
