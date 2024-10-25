package io.metersphere.project.controller;

import io.metersphere.project.controller.param.ProjectApplicationDefinition;
import io.metersphere.project.controller.param.ProjectApplicationRequestDefinition;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.request.ProjectApplicationRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.mapper.TestResourcePoolMapper;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests extends BaseTest {

    private static Plugin plugin;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private ProjectMapper projectMapper;

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
        ProjectApplication request = creatRequest(ProjectApplicationType.TEST_PLAN.TEST_PLAN_CLEAN_REPORT.name(), TIME_TYPE_VALUE);

        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.setTypeValue("4M");
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        // @@异常参数校验
        updatedGroupParamValidateTest(ProjectApplicationDefinition.class, TEST_PLAN_UPDATE_URL);

    }

    //测试计划 - 分享报告配置
    @Test
    @Order(2)
    public void testTestPlanShare() throws Exception {
        //新增
        ProjectApplication request = creatRequest(ProjectApplicationType.TEST_PLAN.TEST_PLAN_SHARE_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(TEST_PLAN_UPDATE_URL, request);
        //更新
        request.setTypeValue("5M");
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
     * ========== 任务中心 start==========
     */
    public static final String TASK_UPDATE_URL = "/project/application/update/task";
    public static final String GET_TASK_URL = "/project/application/task";
    // 任务中心 - 获取配置
    @Test
    @Order(4)
    public void testGetTask() throws Exception {
        ProjectApplicationRequest request = this.getRequest("TASK");
        this.requestPostWithOkAndReturn(GET_TASK_URL, request);
    }
    // 任务中心 - 获取配置
    @Test
    @Order(5)
    public void testUpdateTask() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.TASK.TASK_CLEAN_REPORT.name(), "1D");
        this.requestPostWithOkAndReturn(TASK_UPDATE_URL, request);
    }


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
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_URL_REPEATABLE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);

    }

    //接口测试 - 清理报告配置
    @Test
    @Order(13)
    public void testApiClean() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_CLEAN_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 分享报告配置
    @Test
    @Order(14)
    public void testApiShare() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_SHARE_REPORT.name(), TIME_TYPE_VALUE);
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 执行资源池
    @Test
    @Order(15)
    public void testApiResourcePool() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), "local");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 脚本审核
    @Test
    @Order(16)
    public void testApiReviewer() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_SCRIPT_REVIEWER_ENABLE.name(), "admin");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 自定义误报规则
    @Test
    @Order(17)
    public void testApiErrorReportRule() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_ERROR_REPORT_RULE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 接口变更同步case
    @Test
    @Order(18)
    public void testApiSyncCase() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.API.API_SYNC_CASE.name(), "true");
        this.requestPost(API_UPDATE_URL, request);
    }

    //接口测试 - 获取配置
    @Test
    @Order(19)
    public void testGetApi() throws Exception {
        ProjectApplicationRequest request = this.getRequest("API");
        this.requestPostWithOkAndReturn(GET_API_URL, request);
        Project initProject = new Project();
        initProject.setId("GYQALL");
        initProject.setNum(null);
        initProject.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        initProject.setName("测试项目版本");
        initProject.setDescription("测试项目版本");
        initProject.setCreateUser("admin");
        initProject.setUpdateUser("admin");
        initProject.setCreateTime(System.currentTimeMillis());
        initProject.setUpdateTime(System.currentTimeMillis());
        initProject.setEnable(true);
        initProject.setModuleSetting("[\"apiTest\",\"uiTest\"]");
        initProject.setAllResourcePool(true);
        projectMapper.insertSelective(initProject);
        request.setProjectId("GYQALL");
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
        ProjectApplication request = creatRequest(ProjectApplicationType.CASE.CASE_PUBLIC.name(), "true");
        this.requestPost(CASE_UPDATE_URL, request);
    }

    //用例管理 - 重新提审
    @Test
    @Order(23)
    public void testReview() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.CASE.CASE_RE_REVIEW.name(), "true");
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
    @Order(40)
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
        plugin = basePluginTestService.addJiraPlugin();
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
     * ==========缺陷管理 start==========
     */
    public static final String BUG_UPDATE_URL = "/project/application/update/bug";
    public static final String GET_BUG_URL = "/project/application/bug";
    public static final String GET_BUG_PLATFORM_URL = "/project/application/bug/platform";

    public static final String GET_BUG_PLATFORM_INFO_URL = "/project/application/bug/platform/info";

    //工作台
    @Test
    @Order(29)
    public void testBug() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.BUG.BUG_SYNC.name(), "true");
        this.requestPost(BUG_UPDATE_URL, request);
    }

    @Test
    @Order(30)
    public void testGetBug() throws Exception {
        ProjectApplicationRequest request = this.getRequest("BUG");
        this.requestPostWithOkAndReturn(GET_BUG_URL, request);
    }

    //缺陷管理 - 获取平台下拉列表
    @Test
    @Order(41)
    public void testGetBugPlatform() throws Exception {
        this.requestGetWithOkAndReturn(GET_BUG_PLATFORM_URL + "/100002");
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_BUG_PLATFORM_URL + "/100001");
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }


    //缺陷管理 - 获取平台信息
    @Test
    @Order(32)
    public void testGetBugPlatformInfo() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_BUG_PLATFORM_INFO_URL + "/" + plugin.getId());
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    /**
     * ==========缺陷管理 end==========
     */

    private ProjectApplicationRequest getRequest(String type) {
        ProjectApplicationRequest request = new ProjectApplicationRequest();
        request.setProjectId(PROJECT_ID);
        request.setType(type);
        return request;
    }

    private ProjectApplication creatRequest(String type, String typeValue) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId(PROJECT_ID);
        projectApplication.setType(type);
        projectApplication.setTypeValue(typeValue);
        return projectApplication;

    }


    public static final String UPDATE_BUG_CONFIG_URL = "/project/application/update/bug/sync";
    public static final String GET_BUG_CONFIG_INFO_URL = "/project/application/bug/sync/info";

    @Test
    @Order(34)
    public void testBugConfig() throws Exception {
        ProjectApplication request = creatRequest(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name(), "true");
        this.requestPost(BUG_UPDATE_URL, request);

        Map<String, String> congifs = mockTestData();
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE_BUG_CONFIG_URL + "/project_application_test_id", congifs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        congifs.put("jiraKey", "222");
        MvcResult updateResult = this.requestPostWithOkAndReturn(UPDATE_BUG_CONFIG_URL + "/project_application_test_id", congifs);
        // 获取返回值
        String updateData = updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(updateResultHolder);
        congifs.remove("CRON_EXPRESSION");
        this.requestPostWithOkAndReturn(UPDATE_BUG_CONFIG_URL + "/default-project-2", congifs);

        ProjectApplication afterRequest = creatRequest(ProjectApplicationType.BUG.BUG_SYNC.name() + "_" + ProjectApplicationType.BUG_SYNC_CONFIG.SYNC_ENABLE.name(), "true");
        this.requestPost(BUG_UPDATE_URL, afterRequest);
    }

    private Map<String, String> mockTestData() {
        String jsonConfig = "{\"jiraKey\":\"111\",\"jiraIssueTypeId\":\"10086\",\"jiraStoryTypeId\":\"10010\"}";
        Map<String, String> configs = new HashMap<>();
        configs.put("CRON_EXPRESSION", "0 0 0/1 * * ?");
        configs.put("SYNC_ENABLE", "true");
        configs.put("BUG_PLATFORM_CONFIG", jsonConfig);
        configs.put("MECHANISM", "1");
        configs.put("PLATFORM_KEY", "jira");
        return configs;
    }

    @Test
    @Order(35)
    public void testBugConfigInfo() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_BUG_CONFIG_INFO_URL + "/default-project-2");
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
        ProjectApplication request = creatRequest("bugManagement", "false");
        request.setProjectId("100001100001");
        this.requestPost(BUG_UPDATE_URL, request);
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
        ProjectApplication request = creatRequest(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name(), "true");
        this.requestPost(CASE_UPDATE_URL, request);
        Map<String, String> configs = mockRelatedTestData();
        MvcResult mvcResult = this.requestPostWithOkAndReturn(UPDATE_CASE_RELATED_CONFIG_URL + "/project_application_test_id", configs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);

        //更新
        configs.put("jiraKey", "222");
        MvcResult updateResult = this.requestPostWithOkAndReturn(UPDATE_CASE_RELATED_CONFIG_URL + "/project_application_test_id", configs);
        // 获取返回值
        String updateData = updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder updateResultHolder = JSON.parseObject(updateData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(updateResultHolder);
        ProjectApplication afterRequest = creatRequest(ProjectApplicationType.CASE_RELATED_CONFIG.CASE_RELATED.name() + "_" + ProjectApplicationType.CASE_RELATED_CONFIG.CASE_ENABLE.name(), "true");
        this.requestPost(CASE_UPDATE_URL, afterRequest);
        Map<String, String> falseConfigs = mockCaseFalseRelatedTestData();
        mvcResult = this.requestPostWithOkAndReturn(UPDATE_CASE_RELATED_CONFIG_URL + "/project_application_test_id", falseConfigs);
        // 获取返回值
        returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
    }

    private Map<String, String> mockRelatedTestData() {
        String jsonConfig = "{\"jiraKey\":\"111\",\"jiraIssueTypeId\":\"10086\",\"jiraStoryTypeId\":\"10010\"}";
        Map<String, String> configs = new HashMap<>();
        configs.put("DEMAND_PLATFORM_CONFIG", jsonConfig);
        configs.put("CASE_ENABLE", "true");
        configs.put("CRON_EXPRESSION", "0 0 0 * * ?");
        return configs;
    }

    private Map<String, String> mockCaseFalseRelatedTestData() {
        String jsonConfig = "{\"jiraKey\":\"111\",\"jiraIssueTypeId\":\"10086\",\"jiraStoryTypeId\":\"10010\"}";
        Map<String, String> configs = new HashMap<>();
        configs.put("DEMAND_PLATFORM_CONFIG", jsonConfig);
        configs.put("CASE_ENABLE", "false");
        configs.put("CRON_EXPRESSION", "0 0 0/1 * * ?");
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
    @Resource
    private MockServerClient mockServerClient;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;

    @Test
    @Order(39)
    public void testCheckProjectKey() throws Exception {
        Map<String, String> configs = new HashMap<>();
        configs.put("jiraKey", "Test");
        configs.put("jiraIssueTypeId", "10086");
        configs.put("jiraStoryTypeId", "10010");
        assertErrorCode(this.requestPost(CHECK_PROJECT_KEY_URL + "/" + "test", configs), NOT_FOUND);
        JiraIntegrationConfig integrationConfig = new JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));
        ServiceIntegrationUpdateRequest request = new ServiceIntegrationUpdateRequest();
        request.setEnable(true);
        request.setPluginId(plugin.getId());
        request.setConfiguration(integrationConfigMap);
        request.setOrganizationId("100001");
        this.requestPostWithOkAndReturn("/service/integration/add", request);
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/project/Test"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"id\":\"123456\",\"name\":\"test\"}")
                );
        MvcResult mvcResult = this.requestPostWithOkAndReturn(CHECK_PROJECT_KEY_URL + "/" + plugin.getId(), configs);
        // 获取返回值
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        // 返回请求正常
        Assertions.assertNotNull(resultHolder);
        //测试完清掉
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andPluginIdEqualTo(plugin.getId()).andOrganizationIdEqualTo("100001").andEnableEqualTo(true);
        serviceIntegrationMapper.deleteByExample(example);
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


    @Test
    @Order(40)
    public void testGetProjectBugThirdPartConfig() throws Exception {
        projectApplicationService.getProjectBugThirdPartConfig(DEFAULT_PROJECT_ID);
        projectApplicationService.getProjectBugThirdPartConfig(PROJECT_ID);
    }


    @Test
    @Order(41)
    public void testGetProjectDemandThirdPartConfig() {
        projectApplicationService.getProjectDemandThirdPartConfig(DEFAULT_PROJECT_ID);
        projectApplicationService.getProjectDemandThirdPartConfig(PROJECT_ID);
        projectApplicationService.getPlatformName(DEFAULT_PROJECT_ID);
        projectApplicationService.getPlatformName(PROJECT_ID);
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo("default-project-for-application").andTypeEqualTo("BUG_SYNC_SYNC_ENABLE");
        ProjectApplication record = new ProjectApplication();
        record.setProjectId("default-project-for-application-tmp");
        projectApplicationMapper.updateByExampleSelective(record, example);
        projectApplicationService.getPlatformName("default-project-for-application");
        projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand("default-project-for-application", true);
        ProjectApplicationExample example1 = new ProjectApplicationExample();
        example1.createCriteria().andProjectIdEqualTo("default-project-for-application-tmp").andTypeEqualTo("BUG_SYNC_SYNC_ENABLE");
        record.setProjectId("default-project-for-application");
        record.setTypeValue("false");
        projectApplicationMapper.updateByExampleSelective(record, example1);
        projectApplicationService.getPlatformName("default-project-for-application");
        projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand("default-project-for-application", true);
        ProjectApplicationExample example2 = new ProjectApplicationExample();
        example2.createCriteria().andProjectIdEqualTo("default-project-for-application").andTypeEqualTo("BUG_SYNC_SYNC_ENABLE");
        record.setTypeValue("true");
        projectApplicationMapper.updateByExampleSelective(record, example2);
        projectApplicationService.getPlatformName("default-project-for-application");
        projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand("default-project-for-application", true);
        ProjectApplicationExample example3 = new ProjectApplicationExample();
        example3.createCriteria().andProjectIdEqualTo("default-project-for-application").andTypeEqualTo("BUG_SYNC_PLATFORM_KEY");
        record.setProjectId("default-project-for-application-tmp");
        projectApplicationMapper.updateByExampleSelective(record, example3);
        projectApplicationService.getPlatformName("default-project-for-application");
        projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand("default-project-for-application", true);
        ProjectApplicationExample example4 = new ProjectApplicationExample();
        example4.createCriteria().andProjectIdEqualTo("default-project-for-application-tmp").andTypeEqualTo("BUG_SYNC_PLATFORM_KEY");
        record.setProjectId("default-project-for-application");
        record.setTypeValue("jira");
        projectApplicationMapper.updateByExampleSelective(record, example4);
    }


    @Test
    @Order(99)
    void coverPlatformTest() {
        // 没有配置平台的项目
        try {
            projectApplicationService.getPlatform("default-project-for-application-not-exist", true);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), Translator.get("third_party_not_config"));
        }

        try {
            // 获取需求配置
            projectApplicationService.getPlatform("default-project-for-application", false);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), Translator.get("third_party_not_config"));
        }

        ServiceIntegration record = new ServiceIntegration();
        try {
            // 关闭集成
            record.setId("621103810617345");
            record.setEnable(false);
            serviceIntegrationMapper.updateByPrimaryKeySelective(record);
            projectApplicationService.getPlatform("default-project-for-application", true);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), Translator.get("third_party_not_config"));
        }

        try {
            // 集成Tapd
            record.setEnable(true);
            record.setPluginId("Tapd");
            serviceIntegrationMapper.updateByPrimaryKeySelective(record);
            projectApplicationService.getPlatform("default-project-for-application", true);
        } catch (Exception e) {
            Assertions.assertEquals(e.getMessage(), Translator.get("third_party_not_config"));
        }

        // 获取缺陷同步配置平台
        projectApplicationService.getPlatform("default-project-for-application", true);
        // 获取同步机制
        Assertions.assertTrue(projectApplicationService.isPlatformSyncMethodByIncrement("default-project-for-application"));
        Assertions.assertFalse(projectApplicationService.isPlatformSyncMethodByIncrement("default-project-for-application-not-exist"));
    }

    @Test
    @Order(100)
    public void testResourcePool() {
        // 校验资源池  默认资源池
        String projectId = DEFAULT_PROJECT_ID;
        Map<String, Object> configMap = new HashMap<>();
        projectApplicationService.putResourcePool(projectId, configMap, "apiTest");
        projectApplicationService.putResourcePool(projectId, configMap, "uiTest");
        projectApplicationService.putResourcePool(projectId, configMap, "loadTest");
        //项目与资源池有关系
        TestResourcePoolExample example = new TestResourcePoolExample();
        example.createCriteria().andNameEqualTo("默认资源池");
        List<TestResourcePool> testResourcePools = testResourcePoolMapper.selectByExample(example);
        Assertions.assertFalse(testResourcePools.isEmpty());
        configMap.put(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), testResourcePools.getFirst().getId());
        projectApplicationService.putResourcePool(projectId, configMap, "apiTest");
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(testResourcePools.getFirst().getId());
        testResourcePool.setAllOrg(false);
        testResourcePoolMapper.updateByPrimaryKeySelective(testResourcePool);
        projectApplicationService.putResourcePool(projectId, configMap, "apiTest");


    }
}
