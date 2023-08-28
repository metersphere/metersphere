package io.metersphere.project.controller;

import io.metersphere.project.controller.param.ProjectApplicationDefinition;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.sdk.base.BaseTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests extends BaseTest {


    // 测试计划
    public static final String TEST_PLAN_CLEAN_REPORT_URL = "/project/application/update";
    //清理报告配置
    public static final String TEST_PLAN_CLEAN_REPORT = "APPLICATION_CLEAN_TEST_PLAN_REPORT";
    //分享报告配置
    public static final String TEST_PLAN_SHARE_REPORT = "APPLICATION_TEST_PLAN_SHARE_REPORT";


    /**
     * 应用配置 - 测试计划 清理报告配置
     * @throws Exception
     */
    @Test
    @Order(1)
    public void testTestPlanClean() throws Exception {
        //新增
        ProjectApplication request = creatRequest(TEST_PLAN_CLEAN_REPORT);
        this.requestPost(TEST_PLAN_CLEAN_REPORT_URL, request);

        //更新
        request.setTypeValue("false");
        this.requestPost(TEST_PLAN_CLEAN_REPORT_URL, request);

        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, TEST_PLAN_CLEAN_REPORT_URL);

    }



    /**
     * 应用管理 测试计划 - 分享报告配置
     * @throws Exception
     */
    @Test
    @Order(2)
    public void testUpdateApp() throws Exception {
        //新增
        ProjectApplication request = creatRequest(TEST_PLAN_SHARE_REPORT);
        this.requestPost(TEST_PLAN_CLEAN_REPORT_URL, request);
        //更新
        request.setTypeValue("false");
        this.requestPost(TEST_PLAN_CLEAN_REPORT_URL, request);
    }


    private ProjectApplication creatRequest(String type) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId("project_application_test_id");
        projectApplication.setType(type);
        projectApplication.setTypeValue("true");
        return projectApplication;
    }

}
