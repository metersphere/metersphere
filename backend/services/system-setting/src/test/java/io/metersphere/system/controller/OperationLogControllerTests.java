package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.system.controller.param.OperationLogRequestDefinition;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OperationLogControllerTests extends BaseTest {

    public static final String OPERATION_LOG_LIST = "/operation/log/list";

    public static final String OPTIONS_LIST = "/operation/log/get/options";


    public static final String USER_LIST = "/system/user/list";

    public static final String SYSTEM = "system";
    public static final String ORGANIZATION = "organization";
    public static final String PROJECT = "project";


    /**
     * 系统级别 查询 用例
     *
     * @throws Exception
     */
    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_operation_log_test.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testSystemOperationLogList() throws Exception {
        //系统级别  全部
        OperationLogRequest request = buildParam(SYSTEM);
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        //其他查询条件
        request.setOperUser("admin");
        request.setType("add");
        request.setModule("SYSTEM_PARAMETER_SETTING");
        request.setContent("认证配置");
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        // @@异常参数校验
        updatedGroupParamValidateTest(OperationLogRequestDefinition.class, OPERATION_LOG_LIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPERATION_LOG_LIST, request);

    }


    /**
     * 组织级别 查询 用例
     *
     * @throws Exception
     */
    @Test
    @Order(4)
    public void testOrganizationOperationLogList() throws Exception {
        OperationLogRequest request = buildParam(ORGANIZATION);
        //组织级别 全部
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        //其他查询条件
        request.setOperUser("admin");
        request.setType("add");
        request.setModule("SYSTEM_PARAMETER_SETTING");
        request.setContent("认证配置");
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);


        //组织级别 指定组织查询
        request.setOrganizationIds(Arrays.asList("organization_id_001", "organization_id_002"));
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        // @@异常参数校验
        updatedGroupParamValidateTest(OperationLogRequestDefinition.class, OPERATION_LOG_LIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPERATION_LOG_LIST, request);
    }


    /**
     * 项目级别 查询 用例
     *
     * @throws Exception
     */
    @Test
    @Order(5)
    public void testProjectOperationLogList() throws Exception {
        OperationLogRequest request = buildParam(PROJECT);
        //项目级别 全部
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        //其他查询条件
        request.setOperUser("admin");
        request.setType("add");
        request.setModule("SYSTEM_PARAMETER_SETTING");
        request.setContent("认证配置");
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        //项目级别 指定项目查询
        request.setProjectIds(Arrays.asList("project_id_001", "project_id_002"));
        this.requestPostWithOkAndReturn(OPERATION_LOG_LIST, request);

        // @@异常参数校验
        updatedGroupParamValidateTest(OperationLogRequestDefinition.class, OPERATION_LOG_LIST);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPERATION_LOG_LIST, request);
    }


    @Test
    @Order(2)
    public void testGetOptions() throws Exception {
        this.requestGetWithOkAndReturn(OPTIONS_LIST);

        // @@校验权限
        //requestGetPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPTIONS_LIST);
    }

    @Test
    @Order(3)
    public void testUserList() throws Exception {
        this.requestGetWithOkAndReturn(USER_LIST);

        // @@校验权限
       // requestGetPermissionTest(PermissionConstants.SYSTEM_OPERATING_LOG_READ, OPTIONS_LIST);
    }

    @Test
    @Order(6)
    public void testGetOperationLogParamsError() throws Exception {
        OperationLogRequest request = buildParam(SYSTEM);
        request.setStartTime(1689149059000l);
        request.setEndTime(1689131059000l);
        ResultActions resultActions = this.requestPost(OPERATION_LOG_LIST, request);
        resultActions.andExpect(status().is5xxServerError());
    }


    private OperationLogRequest buildParam(String level) {
        OperationLogRequest request = new OperationLogRequest();
        request.setCurrent(1);
        request.setPageSize(10);
        request.setStartTime(1689131059000l);
        request.setEndTime(1689149059000l);
        request.setLevel(level);
        return request;
    }


}
