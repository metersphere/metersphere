package io.metersphere.project.controller;

import io.metersphere.project.controller.param.ProjectApplicationDefinition;
import io.metersphere.project.controller.param.ProjectApplicationRequestDefinition;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.ProjectApplicationType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests extends BaseTest {
    public static final String PROJECT_ID = "project_application_test_id";
    public static final String TIME_TYPE_VALUE = "3M";


    /**
     * ==========测试计划配置 start==========
     */
    // 测试计划
    public static final String TEST_PLAN_UPDATE_URL = "/project/application/update/test-plan";
    //获取配置
    public static final String GET_TEST_PLAN_URL = "/project/application/test-plan";

    //应用配置 - 测试计划 - 清理报告配置
    @Test
    @Order(1)
    public void testTestPlanClean() throws Exception {
        this.testGetTestPlan();
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_CLEAN_TEST_PLAN_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.setTypeValue("4M");
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, TEST_PLAN_UPDATE_URL);

    }

    //应用管理 - 测试计划 - 分享报告配置
    @Test
    @Order(2)
    public void testTestPlanShare() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_SHARE_TEST_PLAN_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.setTypeValue("5M");
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
    }

    //应用管理 - 测试计划 - 获取配置
    @Test
    @Order(3)
    public void testGetTestPlan() throws Exception {
        //清理报告 + 分享报告
        List<String> types = Arrays.asList(ProjectApplicationType.APPLICATION_CLEAN_TEST_PLAN_REPORT.name(), ProjectApplicationType.APPLICATION_SHARE_TEST_PLAN_REPORT.name());
        ProjectApplicationRequest request = this.getRequest(types);
        this.requestPostWithOkAndReturn(GET_TEST_PLAN_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationRequestDefinition.class, GET_TEST_PLAN_URL);
    }
    /**
     * ==========测试计划配置 end==========
     */


    /**
     * ==========UI测试 start==========
     */
    // UI
    public static final String UI_UPDATE_URL = "/project/application/update/ui";
    //获取配置
    public static final String GET_UI_URL = "/project/application/ui";

    //应用配置 - UI测试 - 清理报告配置
    @Test
    @Order(4)
    public void testUiClean() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_CLEAN_UI_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(UI_UPDATE_URL, request);
        //更新
        request.setTypeValue("4M");
        this.requestPost(UI_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, UI_UPDATE_URL);

    }

    //应用管理 - UI测试 - 分享报告配置
    @Test
    @Order(5)
    public void testUiShare() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_SHARE_UI_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(UI_UPDATE_URL, request);
        //更新
        request.setTypeValue("5M");
        this.requestPost(UI_UPDATE_URL, request);
    }

    //应用管理 - UI测试 - 获取配置
    @Test
    @Order(6)
    public void testGetUi() throws Exception {
        //清理报告 + 分享报告
        List<String> types = Arrays.asList(ProjectApplicationType.APPLICATION_CLEAN_UI_REPORT.name(), ProjectApplicationType.APPLICATION_SHARE_UI_REPORT.name());
        ProjectApplicationRequest request = this.getRequest(types);
        this.requestPostWithOkAndReturn(GET_UI_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationRequestDefinition.class, GET_UI_URL);
    }
    /**
     * ==========UI测试 end==========
     */


    /**
     * ==========性能测试 start==========
     */
    // 性能测试
    public static final String PERFORMANCE_UPDATE_URL = "/project/application/update/performance-test";
    //获取配置
    public static final String GET_PERFORMANCE_URL = "/project/application/performance-test";
    //获取脚本审核人
    public static final String GET_USER_URL = "/project/application/performance-test/user";

    //应用配置 - 性能测试 - 清理报告配置
    @Test
    @Order(7)
    public void testPerformanceClean() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_CLEAN_PERFORMANCE_TEST_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        //更新
        request.setTypeValue("4M");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, PERFORMANCE_UPDATE_URL);

    }

    //应用管理 - 性能测试 - 分享报告配置
    @Test
    @Order(8)
    public void testPerformanceShare() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_SHARE_PERFORMANCE_TEST_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        //更新
        request.setTypeValue("5M");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
    }

    //应用管理 - 性能测试 - 脚本审核
    @Test
    @Order(9)
    public void testPerformanceReviewer() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_PERFORMANCE_TEST_SCRIPT_REVIEWER.name(), "admin");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
    }

    //应用管理 - 性能测试 - 获取配置
    @Test
    @Order(10)
    public void testGetPerformance() throws Exception {
        //清理报告 + 分享报告
        List<String> types = Arrays.asList(ProjectApplicationType.APPLICATION_CLEAN_PERFORMANCE_TEST_REPORT.name(), ProjectApplicationType.APPLICATION_SHARE_PERFORMANCE_TEST_REPORT.name(), ProjectApplicationType.APPLICATION_PERFORMANCE_TEST_SCRIPT_REVIEWER.name());
        ProjectApplicationRequest request = this.getRequest(types);
        this.requestPostWithOkAndReturn(GET_PERFORMANCE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationRequestDefinition.class, GET_PERFORMANCE_URL);
    }

    //应用管理 - 性能测试 - 获取项目成员
    @Test
    @Order(11)
    public void testGetUser() throws Exception {
        this.requestGetWithOkAndReturn(GET_USER_URL + "/default-project-2");
    }
    /**
     * ==========性能测试 end==========
     */


    /**
     * ==========接口测试 start==========
     */
    // 接口测试
    public static final String API_UPDATE_URL = "/project/application/update/api";
    //获取配置
    public static final String GET_API_URL = "/project/application/api";
    //获取脚本审核人
    public static final String GET_API_USER_URL = "/project/application/api/user";
    //获取资源池
    public static final String GET_API_RESOURCE_POOL_URL = "/project/application/api/resource/pool";

    //应用配置 - 接口测试 - URL可重复
    @Test
    @Order(12)
    public void testUrlRepeatable() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_API_URL_REPEATABLE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);

    }

    //应用配置 - 接口测试 - 清理报告配置
    @Test
    @Order(13)
    public void testApiClean() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_CLEAN_API_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 分享报告配置
    @Test
    @Order(14)
    public void testApiShare() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_SHARE_API_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 执行资源池
    @Test
    @Order(15)
    public void testApiResourcePool() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_API_RESOURCE_POOL.name(), "local");
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 脚本审核
    @Test
    @Order(16)
    public void testApiReviewer() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_API_SCRIPT_REVIEWER.name(), "admin");
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 自定义误报规则
    @Test
    @Order(17)
    public void testApiErrorReportRule() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_API_ERROR_REPORT_RULE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 接口变更同步case
    @Test
    @Order(18)
    public void testApiSyncCase() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.APPLICATION_API_SYNC_CASE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //应用管理 - 接口测试 - 获取配置
    @Test
    @Order(19)
    public void testGetApi() throws Exception {
        List<String> types = Arrays.asList(ProjectApplicationType.APPLICATION_API_URL_REPEATABLE.name(), ProjectApplicationType.APPLICATION_CLEAN_API_REPORT.name(), ProjectApplicationType.APPLICATION_SHARE_API_REPORT.name(),
                                            ProjectApplicationType.APPLICATION_API_RESOURCE_POOL.name(), ProjectApplicationType.APPLICATION_API_SCRIPT_REVIEWER.name(), ProjectApplicationType.APPLICATION_API_ERROR_REPORT_RULE.name(),
                                            ProjectApplicationType.APPLICATION_API_SYNC_CASE.name());
        ProjectApplicationRequest request = this.getRequest(types);
        this.requestPostWithOkAndReturn(GET_API_URL, request);
    }

    //应用管理 - 接口测试 - 获取项目成员
    @Test
    @Order(20)
    public void testGetApiUser() throws Exception {
        this.requestGetWithOkAndReturn(GET_API_USER_URL + "/default-project-2");
    }


    //应用管理 - 接口测试 - 获取资源池
    @Test
    @Order(21)
    public void testGetApiResourcePool() throws Exception {
        this.requestGetWithOkAndReturn(GET_API_RESOURCE_POOL_URL + "/100001");
    }
    /**
     * ==========接口测试 end==========
     */


    private ProjectApplicationRequest getRequest(List<String> types) {
        ProjectApplicationRequest request = new ProjectApplicationRequest();
        request.setProjectId(PROJECT_ID);
        request.setTypes(types);
        return request;
    }

    private ProjectApplication creatRequest(String type, String typeValue) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(PROJECT_ID);
        projectApplication.setType(type);
        projectApplication.setTypeValue(typeValue);
        return projectApplication;
    }

}
