package io.metersphere.project.controller;

import io.metersphere.project.controller.param.ProjectApplicationDefinition;
import io.metersphere.project.controller.param.ProjectApplicationRequestDefinition;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.request.PluginUpdateRequest;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.service.PluginService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests extends BaseTest {

    private static Plugin plugin;
    @Resource
    private PluginService pluginService;

    public static final String PROJECT_ID = "project_application_test_id";
    public static final String TIME_TYPE_VALUE = "3M";


    /**
     * ==========测试计划配置 start==========
     */
    public static final String TEST_PLAN_UPDATE_URL = "/project/application/update/test-plan";
    public static final String GET_TEST_PLAN_URL = "/project/application/test-plan";

    //测试计划 - 清理报告配置
    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_application_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testTestPlanClean() throws Exception {
        this.testGetTestPlan();
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name()), TIME_TYPE_VALUE);

        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.get(0).setTypeValue("4M");
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, TEST_PLAN_UPDATE_URL);

    }

    //测试计划 - 分享报告配置
    @Test
    @Order(2)
    public void testTestPlanShare() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.TEST_PLAN.TEST_PLAN_SHARE_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.get(0).setTypeValue("5M");
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
    }

    //测试计划 - 获取配置
    @Test
    @Order(3)
    public void testGetTestPlan() throws Exception {
        //清理报告 + 分享报告
        ProjectApplicationRequest request = this.getRequest("TEST_PLAN");
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
    public static final String UI_UPDATE_URL = "/project/application/update/ui";
    public static final String GET_UI_URL = "/project/application/ui";
    public static final String GET_UI_RESOURCE_POOL_URL = "/project/application/ui/resource/pool";

    //UI测试 - 清理报告配置
    @Test
    @Order(4)
    public void testUiClean() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.UI.UI_CLEAN_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(UI_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, UI_UPDATE_URL);

    }

    //UI测试 - 分享报告配置
    @Test
    @Order(5)
    public void testUiShare() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.UI.UI_SHARE_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(UI_UPDATE_URL, request);
        //更新
        request.get(0).setTypeValue("5M");
        this.requestPost(UI_UPDATE_URL, request);
    }

    //UI测试 - 执行资源池
    @Test
    @Order(5)
    public void testUiResourcePool() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.UI.UI_RESOURCE_POOL.name()), "local");
        this.requestPost(UI_UPDATE_URL, request);
    }

    //UI测试 - 获取配置
    @Test
    @Order(6)
    public void testGetUi() throws Exception {
        //清理报告 + 分享报告
        ProjectApplicationRequest request = this.getRequest("UI");
        this.requestPostWithOkAndReturn(GET_UI_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationRequestDefinition.class, GET_UI_URL);
    }

    //UI测试 - 获取资源池
    @Test
    @Order(6)
    public void testGetUiResourcePool() throws Exception {
        this.requestGetWithOkAndReturn(GET_UI_RESOURCE_POOL_URL + "/" + DEFAULT_PROJECT_ID);
    }
    /**
     * ==========UI测试 end==========
     */


    /**
     * ==========性能测试 start==========
     */
    public static final String PERFORMANCE_UPDATE_URL = "/project/application/update/performance-test";
    public static final String GET_PERFORMANCE_URL = "/project/application/performance-test";
    public static final String GET_USER_URL = "/project/application/performance-test/user";

    //性能测试 - 清理报告配置
    @Test
    @Order(7)
    public void testPerformanceClean() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.PERFORMANCE_TEST.PERFORMANCE_TEST_CLEAN_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        //更新
        request.get(0).setTypeValue("4M");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, PERFORMANCE_UPDATE_URL);

    }

    //性能测试 - 分享报告配置
    @Test
    @Order(8)
    public void testPerformanceShare() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.PERFORMANCE_TEST.PERFORMANCE_TEST_SHARE_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
        //更新
        request.get(0).setTypeValue("5M");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
    }

    //性能测试 - 脚本审核
    @Test
    @Order(9)
    public void testPerformanceReviewer() throws Exception {
        //新增
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.PERFORMANCE_TEST.PERFORMANCE_TEST_SCRIPT_REVIEWER.name()), "admin");
        this.requestPost(PERFORMANCE_UPDATE_URL, request);
    }

    //性能测试 - 获取配置
    @Test
    @Order(10)
    public void testGetPerformance() throws Exception {
        //清理报告 + 分享报告
        ProjectApplicationRequest request = this.getRequest("PERFORMANCE_TEST");
        this.requestPostWithOkAndReturn(GET_PERFORMANCE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationRequestDefinition.class, GET_PERFORMANCE_URL);
    }

    //性能测试 - 获取项目成员
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
    public static final String API_UPDATE_URL = "/project/application/update/api";
    public static final String GET_API_URL = "/project/application/api";
    public static final String GET_API_USER_URL = "/project/application/api/user";
    public static final String GET_API_RESOURCE_POOL_URL = "/project/application/api/resource/pool";

    //接口测试 - URL可重复
    @Test
    @Order(12)
    public void testUrlRepeatable() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_URL_REPEATABLE.name()), "true");
        this.requestPost(API_UPDATE_URL, request);

    }

    //接口测试 - 清理报告配置
    @Test
    @Order(13)
    public void testApiClean() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_CLEAN_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 分享报告配置
    @Test
    @Order(14)
    public void testApiShare() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_SHARE_REPORT.name()), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 执行资源池
    @Test
    @Order(15)
    public void testApiResourcePool() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_RESOURCE_POOL.name()), "local");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 脚本审核
    @Test
    @Order(16)
    public void testApiReviewer() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_SCRIPT_REVIEWER.name()), "admin");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 自定义误报规则
    @Test
    @Order(17)
    public void testApiErrorReportRule() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_ERROR_REPORT_RULE.name()), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 接口变更同步case
    @Test
    @Order(18)
    public void testApiSyncCase() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.API.API_SYNC_CASE.name()), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 获取配置
    @Test
    @Order(19)
    public void testGetApi() throws Exception {
        ProjectApplicationRequest request = this.getRequest("API");
        this.requestPostWithOkAndReturn(GET_API_URL, request);
    }

    //接口测试 - 获取项目成员
    @Test
    @Order(20)
    public void testGetApiUser() throws Exception {
        this.requestGetWithOkAndReturn(GET_API_USER_URL + "/default-project-2");
    }


    //接口测试 - 获取资源池
    @Test
    @Order(21)
    public void testGetApiResourcePool() throws Exception {
        this.requestGetWithOkAndReturn(GET_API_RESOURCE_POOL_URL + "/" + DEFAULT_PROJECT_ID);
    }
    /**
     * ==========接口测试 end==========
     */


    /**
     * ==========用例管理 start==========
     */
    public static final String CASE_UPDATE_URL = "/project/application/update/case";
    public static final String GET_CASE_URL = "/project/application/case";
    public static final String GET_PLATFORM_URL = "/project/application/case/platform";

    public static final String GET_PLATFORM_INFO_URL = "/project/application/case/platform/info";

    //用例管理 - 公共用例库
    @Test
    @Order(22)
    public void testCasePublic() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.CASE.CASE_PUBLIC.name()), "true");
        this.requestPost(CASE_UPDATE_URL, request);
    }

    //用例管理 - 重新提审
    @Test
    @Order(23)
    public void testReview() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.CASE.CASE_RE_REVIEW.name()), "true");
        this.requestPost(CASE_UPDATE_URL, request);
    }

    //用例管理 - 获取配置
    @Test
    @Order(24)
    public void testGetCase() throws Exception {
        ProjectApplicationRequest request = this.getRequest("CASE");
        this.requestPostWithOkAndReturn(GET_CASE_URL, request);
    }

    //用例管理 - 获取平台下拉列表
    @Test
    @Order(25)
    public void testGetPlatform() throws Exception {
        this.requestGetWithOkAndReturn(GET_PLATFORM_URL + "/100002");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_PLATFORM_URL + "/100001");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    //用例管理 - 获取平台信息
    @Test
    @Order(26)
    public void testGetPlatformInfo() throws Exception {
        plugin = addPlugin();
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_PLATFORM_INFO_URL + "/" + plugin.getId());
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * ==========用例管理 end==========
     */


    /**
     * ==========工作台 start==========
     */
    public static final String WORKSTATION_UPDATE_URL = "/project/application/update/workstation";
    public static final String GET_WORKSTATION_URL = "/project/application/workstation";

    //工作台
    @Test
    @Order(27)
    public void testWorkstation() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.WORKSTATION.WORKSTATION_SYNC_RULE.name()), "true");
        this.requestPost(WORKSTATION_UPDATE_URL, request);
    }

    @Test
    @Order(28)
    public void testGetWorkstation() throws Exception {
        ProjectApplicationRequest request = this.getRequest("WORKSTATION");
        this.requestPostWithOkAndReturn(GET_WORKSTATION_URL, request);
    }
    /**
     * ==========工作台 end==========
     */


    /**
     * ==========缺陷管理 start==========
     */
    public static final String ISSUE_UPDATE_URL = "/project/application/update/issue";
    public static final String GET_ISSUE_URL = "/project/application/issue";
    public static final String GET_ISSUE_PLATFORM_URL = "/project/application/issue/platform";

    public static final String GET_ISSUE_PLATFORM_INFO_URL = "/project/application/issue/platform/info";

    //工作台
    @Test
    @Order(29)
    public void testIssue() throws Exception {
        List<ProjectApplication> request = creatRequest(Arrays.asList(ProjectApplicationType.ISSUE.ISSUE_SYNC.name()), "true");
        this.requestPost(ISSUE_UPDATE_URL, request);
    }

    @Test
    @Order(30)
    public void testGetIssue() throws Exception {
        ProjectApplicationRequest request = this.getRequest("ISSUE");
        this.requestPostWithOkAndReturn(GET_ISSUE_URL, request);
    }

    //缺陷管理 - 获取平台下拉列表
    @Test
    @Order(31)
    public void testGetIssuePlatform() throws Exception {
        this.requestGetWithOkAndReturn(GET_ISSUE_PLATFORM_URL + "/100002");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_ISSUE_PLATFORM_URL + "/100001");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    //缺陷管理 - 获取平台信息
    @Test
    @Order(32)
    public void testGetIssuePlatformInfo() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_ISSUE_PLATFORM_INFO_URL + "/" + plugin.getId());
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * ==========缺陷管理 end==========
     */


    public Plugin addPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/metersphere-jira-plugin-3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试插件1");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }


    private ProjectApplicationRequest getRequest(String type) {
        ProjectApplicationRequest request = new ProjectApplicationRequest();
        request.setProjectId(PROJECT_ID);
        request.setType(type);
        return request;
    }

    private List<ProjectApplication> creatRequest(List<String> type, String typeValue) {
        List<ProjectApplication> list = new ArrayList<>();
        type.forEach(t -> {
            ProjectApplication projectApplication = new ProjectApplication();
            projectApplication.setProjectId(PROJECT_ID);
            projectApplication.setType(t);
            projectApplication.setTypeValue(typeValue);
            list.add(projectApplication);
        });
        return list;
    }


    public static final String UPDATE_ISSUE_CONFIG_URL = "/project/application/update/issue/sync";
    public static final String GET_ISSUE_CONFIG_INFO_URL = "/project/application/issue/sync/info";

    @Test
    @Order(34)
    public void testIssueConfig() throws Exception {
        Map<String, String> congifs = mockTeseData();
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE_ISSUE_CONFIG_URL + "/default-project-2", congifs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //更新
        congifs.put("jiraKey", "222");
        MvcResult updateResult = this.requestPostWithOkAndReturn(UPDATE_ISSUE_CONFIG_URL + "/default-project-2", congifs);
        // 获取返回值
        String updateData = updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(updateResultHolder);

        congifs.remove("CRON_EXPRESSION");
        this.requestPostWithOkAndReturn(UPDATE_ISSUE_CONFIG_URL + "/default-project-2", congifs);
    }

    private Map<String, String> mockTeseData() {
        Map<String, String> configs = new HashMap<>();
        configs.put("platform", "jira");
        configs.put("jiraKey", "111");
        configs.put("jiraIssueTypeId", "10086");
        configs.put("jiraStoryTypeId", "10010");
        configs.put("CRON_EXPRESSION", "0 0 0/1 * * ?");
        configs.put("ENABLE", "true");
        configs.put("MECHANISM", "1");
        return configs;
    }

    @Test
    @Order(35)
    public void testIssueConfigInfo() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_ISSUE_CONFIG_INFO_URL + "/default-project-2");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    public static final String GET_MODULE_SETTING_URL = "/project/application/module-setting";

    @Test
    @Order(36)
    public void testGetModuleSetting() throws Exception {
        this.requestGetWithOkAndReturn(GET_MODULE_SETTING_URL + "/100001100002");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_MODULE_SETTING_URL + "/100001100001");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //更新
        List<ProjectApplication> request = creatRequest(Arrays.asList("bugManagement"), "false");
        request.get(0).setProjectId("100001100001");
        this.requestPost(ISSUE_UPDATE_URL, request);
        MvcResult updateMvcResult = this.requestGetWithOkAndReturn(GET_MODULE_SETTING_URL + "/100001100001");
        // 获取返回值
        String updateReturnData = updateMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateReturnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(updateResultHolder);
    }


    public static final String UPDATE_CASE_RELATED_CONFIG_URL = "/project/application/update/case/related";
    public static final String GET_CASE_RELATED_CONFIG_INFO_URL = "/project/application/case/related/info";

    @Test
    @Order(37)
    public void testCaseRelatedConfig() throws Exception {
        Map<String, String> congifs = mockRelatedTeseData();
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE_CASE_RELATED_CONFIG_URL + "/default-project-2", congifs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //更新
        congifs.put("jiraKey", "222");
        MvcResult updateResult = this.requestPostWithOkAndReturn(UPDATE_CASE_RELATED_CONFIG_URL + "/default-project-2", congifs);
        // 获取返回值
        String updateData = updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(updateResultHolder);
    }

    private Map<String, String> mockRelatedTeseData() {
        Map<String, String> configs = new HashMap<>();
        configs.put("platform", "jira");
        configs.put("jiraKey", "111");
        configs.put("jiraIssueTypeId", "10086");
        configs.put("jiraStoryTypeId", "10010");
        configs.put("ENABLE", "true");
        return configs;
    }


    @Test
    @Order(38)
    public void testCaseRelatedConfigInfo() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_CASE_RELATED_CONFIG_INFO_URL + "/default-project-2");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    public static final String CHECK_PROJECT_KEY_URL = "/project/application/validate";

    @Test
    @Order(39)
    public void testCheckProjectKey() throws Exception {
        Map<String, String> configs = new HashMap<>();
        configs.put("jiraKey", "Test");
        configs.put("jiraIssueTypeId", "10086");
        configs.put("jiraStoryTypeId", "10010");
        assertErrorCode(this.requestPost(CHECK_PROJECT_KEY_URL + "/" + plugin.getId(), configs), NOT_FOUND);
        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setEnable(true);
        request.setPluginId(plugin.getId());
        request.setConfiguration(integrationConfigMap);
        request.setOrganizationId("100001100001");
        this.requestPostWithOkAndReturn("/service/integration/add", request);

        MvcResult mvcResult = this.requestPostWithOkAndReturn(CHECK_PROJECT_KEY_URL + "/" + plugin.getId(), configs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    @Getter
    @Setter
    public class JiraIntegrationConfig {
        private String account;
        private String password;
        private String token;
        private String authType;
        private String address;
        private String version;
    }
}
