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
        this.testGetTestPlan();
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
