package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.service.BaseEnvTestService;
import io.metersphere.api.service.BaseResourcePoolTestService;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.domain.CustomFunction;
import io.metersphere.project.domain.ProjectTestResourcePool;
import io.metersphere.project.domain.ProjectTestResourcePoolExample;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRequest;
import io.metersphere.project.dto.customfunction.request.CustomFunctionRunRequest;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.mapper.ProjectTestResourcePoolMapper;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.sdk.constants.InternalUser;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.service.PluginService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.system.controller.handler.result.MsHttpResultCode.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  17:07
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiTestControllerTests extends BaseTest {

    private static final String BASE_PATH = "/api/test/";
    protected static final String PROTOCOL_LIST = "protocol/{0}";
    protected static final String MOCK = "mock";
    protected static final String CUSTOM_FUNC_RUN = "custom/func/run";
    protected static final String PLUGIN_FORM_OPTION = "plugin/form/option";
    protected static final String PLUGIN_SCRIPT = "plugin/script/{0}";
    protected static final String ENV_LIST = "env-list/{0}";
    protected static final String ENVIRONMENT = "environment/{0}";
    protected static final String COMMON_SCRIPT = "common-script/{scriptId}";

    @Resource
    private BaseResourcePoolTestService baseResourcePoolTestService;
    @Resource
    private PluginService pluginService;
    @Resource
    private BasePluginTestService basePluginTestService;
    @Resource
    private BaseEnvTestService baseEnvTestService;
    @Resource
    private ProjectTestResourcePoolMapper projectTestResourcePoolMapper;
    @Resource
    private CustomFunctionService customFunctionService;

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Test
    public void getProtocols() throws Exception {
        // @@请求成功
        this.requestGetWithOk(PROTOCOL_LIST, this.DEFAULT_ORGANIZATION_ID).andReturn();
    }


    private MvcResult responsePost(String url, Object param) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    public void getMock() throws Exception {
        // @@请求成功
        MvcResult mvcResult = responsePost(BASE_PATH + MOCK, "@integer");
        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void runCustomFunc() throws Exception {
        mockPost("/api/debug", "");
        // 初始化资源池
        baseResourcePoolTestService.initProjectResourcePool();

        CustomFunctionRunRequest request = new CustomFunctionRunRequest();
        request.setReportId(IDGenerator.nextStr());
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setType(ScriptLanguageType.BEANSHELL.name());
        request.setScript("""
                    log.info("========");
                    log.info("${test}");
                """);
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);

        KeyValueParam keyValueParam = new KeyValueParam();
        keyValueParam.setKey("test");
        keyValueParam.setValue("value");
        request.setParams(List.of(keyValueParam));
        // @@请求成功
        this.requestPostWithOk(CUSTOM_FUNC_RUN, request);
        // @@校验权限
        requestPostPermissionTest(PermissionConstants.PROJECT_CUSTOM_FUNCTION_EXECUTE, CUSTOM_FUNC_RUN, request);
    }

    @Test
    @Order(10)
    public void getFormOptions() throws Exception {
        ApiTestPluginOptionRequest request = new ApiTestPluginOptionRequest();
        request.setOrgId(DEFAULT_ORGANIZATION_ID);
        request.setQueryParam(new HashMap<>());
        request.setPluginId("aaa");
        request.setOptionMethod("testGetApiPluginSelectOption");
        assertErrorCode(this.requestPost(PLUGIN_FORM_OPTION, request), NOT_FOUND);
        Plugin jiraPlugin = basePluginTestService.addJiraPlugin();
        request.setPluginId(jiraPlugin.getId());
        assertErrorCode(this.requestPost(PLUGIN_FORM_OPTION, request), NOT_FOUND);
        // @@请求成功
        Plugin plugin = addOptionTestPlugin();
        request.setPluginId(plugin.getId());
        MvcResult mvcResult = this.requestPostWithOkAndReturn(PLUGIN_FORM_OPTION, request);
        List<ApiPluginSelectOption> options = getResultDataArray(mvcResult, ApiPluginSelectOption.class);
        Assertions.assertEquals(options.size(), 1);

        basePluginTestService.deleteJiraPlugin();
        pluginService.delete(plugin.getId());

        // @@校验权限
        requestPostPermissionsTest(new ArrayList<>() {{
            add(PermissionConstants.PROJECT_API_DEFINITION_READ);
            add(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            add(PermissionConstants.PROJECT_API_DEBUG_READ);
            add(PermissionConstants.PROJECT_API_SCENARIO_READ);
        }}, PLUGIN_FORM_OPTION, request);
    }

    @Test
    @Order(11)
    public void getApiProtocolScript() throws Exception {
        assertErrorCode(this.requestGet(PLUGIN_SCRIPT, "aa"), NOT_FOUND);
        Plugin jiraPlugin = basePluginTestService.addJiraPlugin();
        assertErrorCode(this.requestGet(PLUGIN_SCRIPT, jiraPlugin.getId()), NOT_FOUND);
        // @@请求成功
        Plugin plugin = addTcpTestPlugin();
        MvcResult mvcResult = this.requestGetWithOkAndReturn(PLUGIN_SCRIPT, plugin.getId());
        Object resultData = getResultData(mvcResult, Object.class);
        Assertions.assertNotNull(resultData);

        basePluginTestService.deleteJiraPlugin();
        pluginService.delete(plugin.getId());

        // @@校验权限
        requestGetPermissionsTest(new ArrayList<>() {{
            add(PermissionConstants.PROJECT_API_DEFINITION_READ);
            add(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            add(PermissionConstants.PROJECT_API_DEBUG_READ);
            add(PermissionConstants.PROJECT_API_SCENARIO_READ);
        }}, PLUGIN_SCRIPT, "11");
    }

    public Plugin addOptionTestPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/dubbo-option-test-sampler-v3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试获取选项插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

    public Plugin addTcpTestPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/tcp-sampler-v3.x.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试TCP插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

    @Test
    public void getEnvList() throws Exception {
        // @@请求成功
        this.requestGet(ENV_LIST, DEFAULT_PROJECT_ID);
    }

    @Test
    public void getEnvironmentConfig() throws Exception {
        Environment environment = baseEnvTestService.initEnv("111");
        MvcResult mvcResult = this.requestGetAndReturn(ENVIRONMENT, environment.getId());
        EnvironmentConfig environmentConfig = getResultData(mvcResult, EnvironmentConfig.class);
        Assertions.assertNull(environmentConfig.getPreProcessorConfig());
        Assertions.assertNull(environmentConfig.getPostProcessorConfig());
        Assertions.assertNull(environmentConfig.getAssertionConfig());
    }

    @Test
    public void getCommonScriptInfo() throws Exception {
        MvcResult mvcResult = this.requestGetAndReturn(COMMON_SCRIPT, "111");
        Assertions.assertNull(parseResponse(mvcResult).get("data"));

        // 创建测试数据
        CustomFunctionRequest request = new CustomFunctionRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName(UUID.randomUUID().toString());
        request.setStatus("UNDERWAY");
        request.setScript("script");
        // 执行方法调用
        request.setName(UUID.randomUUID().toString());
        CustomFunction customFunction = customFunctionService.add(request, InternalUser.ADMIN.getValue());
        mvcResult = this.requestGetAndReturn(COMMON_SCRIPT, customFunction.getId());
        CommonScriptInfo resultData = getResultData(mvcResult, CommonScriptInfo.class);

        Assertions.assertEquals(resultData.getScript(), request.getScript());
        Assertions.assertEquals(resultData.getName(), request.getName());

        // @@校验权限
        requestGetPermissionsTest(new ArrayList<>() {{
            add(PermissionConstants.PROJECT_API_DEFINITION_READ);
            add(PermissionConstants.PROJECT_API_DEFINITION_CASE_READ);
            add(PermissionConstants.PROJECT_API_DEBUG_READ);
            add(PermissionConstants.PROJECT_API_SCENARIO_READ);
        }}, COMMON_SCRIPT, "11");
    }

    @Test
    public void getPoolOption() throws Exception {
        // @@请求成功
        this.requestGet("/pool-option/" + DEFAULT_PROJECT_ID);
    }

    @Test
    public void getPoolId() throws Exception {
        // @@请求成功
        this.requestGet("/get-pool/" + DEFAULT_PROJECT_ID);
        ProjectTestResourcePoolExample example = new ProjectTestResourcePoolExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<ProjectTestResourcePool> projectTestResourcePools = projectTestResourcePoolMapper.selectByExample(example);
        projectTestResourcePoolMapper.deleteByExample(example);
        this.requestGet("/get-pool/" + DEFAULT_PROJECT_ID);
        projectTestResourcePoolMapper.batchInsert(projectTestResourcePools);
    }

    @Test
    public void fileDownloadTestSuccess() throws Exception {
        this.requestPostAndReturn(BASE_PATH + "download", "test");

    }
}
