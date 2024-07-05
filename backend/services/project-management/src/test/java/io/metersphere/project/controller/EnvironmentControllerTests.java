package io.metersphere.project.controller;


import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.api.assertion.*;
import io.metersphere.project.api.assertion.body.*;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.project.dto.environment.*;
import io.metersphere.project.dto.environment.common.CommonParams;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.host.Host;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.processors.*;
import io.metersphere.project.dto.environment.processors.pre.UiPreScript;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.constants.DefaultRepositoryDir;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.request.PluginUpdateRequest;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.mapper.PluginScriptMapper;
import io.metersphere.system.service.PluginService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.metersphere.sdk.constants.InternalUserRole.ADMIN;
import static io.metersphere.sdk.constants.PermissionConstants.PROJECT_ENVIRONMENT_READ;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentControllerTests extends BaseTest {

    private static final String prefix = "/project/environment";
    private static final String add = prefix + "/add";
    private static final String get = prefix + "/get/";
    private static final String update = prefix + "/update";
    private static final String delete = prefix + "/delete/";
    private static final String list = prefix + "/list";
    private static final String getEnTry = prefix + "/get/entry";
    private static final String importEnv = prefix + "/import";
    private static final String exportEnv = prefix + "/export";
    private static final String POS_URL = prefix + "/edit/pos";

    private static final String validate = prefix + "/database/validate";
    private static final String getOptions = prefix + "/database/driver-options/";
    private static final String SCRIPTS = prefix + "/scripts/{0}";
    private static final String listOption = prefix + "/get-options/";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static String MOCKID;
    @Resource
    private MockMvc mockMvc;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
    @Resource
    private PluginService pluginService;
    @Resource
    private PluginScriptMapper pluginScriptMapper;
    @Resource
    private EnvironmentService environmentService;
    @Resource
    private ExtEnvironmentMapper extEnvironmentMapper;
    @Value("${spring.datasource.url}")
    private String dburl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public static <T> T parseObjectFromMvcResult(MvcResult mvcResult, Class<T> parseClass) {
        try {
            String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
            ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
            //返回请求正常
            Assertions.assertNotNull(resultHolder);
            return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), parseClass);
        } catch (Exception ignore) {

        }
        return null;
    }

    private MvcResult responseGet(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
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

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    protected MvcResult requestMultipartWithOk(String url, MultiValueMap<String, Object> paramMap, String projectId) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header(SessionConstants.CURRENT_PROJECT, projectId);
        return mockMvc.perform(header)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
    }

    protected MvcResult requestMultipart(String url, MultiValueMap<String, Object> paramMap, ResultMatcher resultMatcher) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN");
        return mockMvc.perform(header)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andReturn();
    }

    private MockMultipartHttpServletRequestBuilder getMultipartRequestBuilderWithParam(String url, MultiValueMap<String, Object> paramMap) {
        MockMultipartHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.multipart(getBasePath() + url);
        paramMap.forEach((key, value) -> {
            List list = value;
            for (Object o : list) {
                if (o instanceof List fileList) {
                    fileList.forEach(o1 -> {
                        if (o1 instanceof MockMultipartFile mockMultipartFile) {
                            try {
                                MockMultipartFile mockMultipartFile1 = null;
                                mockMultipartFile1 = new MockMultipartFile(key, mockMultipartFile.getOriginalFilename(),
                                        MediaType.APPLICATION_OCTET_STREAM_VALUE, mockMultipartFile.getBytes());
                                requestBuilder.file(mockMultipartFile1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    });
                } else {
                    MockMultipartFile multipartFile = null;
                    multipartFile = new MockMultipartFile(key, null,
                            MediaType.APPLICATION_JSON_VALUE, o.toString().getBytes());
                    requestBuilder.file(multipartFile);
                }
            }
        });
        return requestBuilder;

    }

    private CommonParams createCommonParams() {
        CommonParams commonParams = new CommonParams();
        commonParams.setRequestTimeout(6000L);
        commonParams.setResponseTimeout(6000L);
        return commonParams;
    }

    private List<HttpConfig> createHttpConfig() {
        List<HttpConfig> httpConfigs = new ArrayList<>();
        HttpConfig httpConfig = new HttpConfig();
        List<KeyValueEnableParam> headers = new ArrayList<>();
        KeyValueEnableParam keyValue = new KeyValueEnableParam();
        keyValue.setKey("key");
        keyValue.setValue("value");
        headers.add(keyValue);
        httpConfig.setHeaders(headers);
        httpConfig.setHostname("http://www.baidu.com");

        httpConfigs.add(httpConfig);
        return httpConfigs;
    }


    private List<CommonVariables> createCommonVariables(int count) {
        List<CommonVariables> commonVariables = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CommonVariables commonVariable = new CommonVariables();
            commonVariable.setKey("key" + i);
            commonVariable.setValue("value" + i);
            commonVariable.setDescription("description" + i);
            commonVariables.add(commonVariable);
        }
        return commonVariables;
    }

    private List<DataSource> createDataSources() {
        List<DataSource> dataSources = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setDbUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai");
        dataSource.setId("1");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setPoolMax(10L);
        dataSource.setTimeout(1000L);
        dataSource.setDataSource("mysql");
        dataSources.add(dataSource);
        return dataSources;
    }

    private HostConfig crateHostConfig() {
        HostConfig hostConfig = new HostConfig();
        hostConfig.setEnable(true);
        Host host = new Host();
        host.setDomain("domain");
        host.setDescription("description");
        host.setIp("ip");
        hostConfig.setHosts(List.of(host));
        return hostConfig;
    }

    private EnvProcessorConfig createEnvironmentProcessorConfig() {
        EnvProcessorConfig environmentPreScript = new EnvProcessorConfig();
        environmentPreScript.setApiProcessorConfig(createApiEnvProcessorConfig());
        return environmentPreScript;
    }

    private ApiEnvProcessorConfig createApiEnvProcessorConfig() {
        ApiEnvProcessorConfig apiScript = new ApiEnvProcessorConfig();
        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setScript("script");
        scriptProcessor.setName("测试计划级别脚本");
        SQLProcessor sqlProcessor = new SQLProcessor();
        sqlProcessor.setDataSourceId("dataSourceId");
        sqlProcessor.setQueryTimeout(1000L);
        ApiEnvPlanProcessorConfig apiEnvPlanProcessorConfig = new ApiEnvPlanProcessorConfig();
        apiEnvPlanProcessorConfig.setProcessors(List.of(scriptProcessor, sqlProcessor));
        apiScript.setPlanProcessorConfig(apiEnvPlanProcessorConfig);
        EnvScenarioScriptProcessor scenarioScript = new EnvScenarioScriptProcessor();
        scenarioScript.setScript("script");
        scenarioScript.setName("场景级别脚本");
        ApiEnvScenarioProcessorConfig apiEnvScenarioProcessorConfig = new ApiEnvScenarioProcessorConfig();
        apiEnvScenarioProcessorConfig.setProcessors(List.of(scenarioScript, sqlProcessor));
        apiScript.setScenarioProcessorConfig(apiEnvScenarioProcessorConfig);
        EnvRequestScriptProcessor stepScript = new EnvRequestScriptProcessor();
        stepScript.setScript("script");
        stepScript.setName("步骤级别前置脚本前");
        stepScript.setBeforeStepScript(true);
        stepScript.setIgnoreProtocols(List.of("HTTP"));
        EnvRequestScriptProcessor stepScriptAfter = new EnvRequestScriptProcessor();
        stepScriptAfter.setScript("script");
        stepScriptAfter.setName("步骤级别前置脚本后");
        stepScriptAfter.setBeforeStepScript(false);
        stepScriptAfter.setIgnoreProtocols(List.of("HTTP"));
        ApiEnvRequestProcessorConfig apiEnvRequestProcessorConfig = new ApiEnvRequestProcessorConfig();
        apiEnvRequestProcessorConfig.setProcessors(List.of(stepScript, sqlProcessor, stepScriptAfter));
        apiScript.setRequestProcessorConfig(apiEnvRequestProcessorConfig);
        return apiScript;
    }

    private UiPreScript createUiPreScript() {
        UiPreScript uiPreScript = new UiPreScript();
        uiPreScript.setJsrType(true);
        uiPreScript.setJsrSetVariable(true);
        uiPreScript.setVariableName("variableName");
        uiPreScript.setValue(true);
        return uiPreScript;
    }

    public static List<MsAssertion> getGeneralAssertions() {
        List<MsAssertion> assertions = new ArrayList<>();
        MsResponseCodeAssertion responseCodeAssertion = new MsResponseCodeAssertion();
        responseCodeAssertion.setCondition(MsAssertionCondition.EMPTY.name());
        responseCodeAssertion.setExpectedValue("value");
        responseCodeAssertion.setName("name");
        assertions.add(responseCodeAssertion);

        MsResponseHeaderAssertion responseHeaderAssertion = new MsResponseHeaderAssertion();
        MsResponseHeaderAssertion.ResponseHeaderAssertionItem responseHeaderAssertionItem = new MsResponseHeaderAssertion.ResponseHeaderAssertionItem();
        responseHeaderAssertionItem.setHeader("header");
        responseHeaderAssertionItem.setExpectedValue("value");
        responseHeaderAssertionItem.setCondition(MsAssertionCondition.EMPTY.name());
        responseHeaderAssertion.setAssertions(List.of(responseHeaderAssertionItem));
        assertions.add(responseHeaderAssertion);

        MsResponseBodyAssertion regexResponseBodyAssertion = new MsResponseBodyAssertion();
        regexResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.REGEX.name());
        MsRegexAssertion regexAssertion = new MsRegexAssertion();

        MsRegexAssertionItem msRegexAssertionItem = new MsRegexAssertionItem();
        msRegexAssertionItem.setExpression("^test");
        regexAssertion.setAssertions(List.of(msRegexAssertionItem));
        regexResponseBodyAssertion.setRegexAssertion(regexAssertion);
        assertions.add(regexResponseBodyAssertion);

        MsResponseBodyAssertion documentResponseBodyAssertion = new MsResponseBodyAssertion();
        documentResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.DOCUMENT.name());
        MsDocumentAssertion msDocumentAssertion = new MsDocumentAssertion();
        documentResponseBodyAssertion.setDocumentAssertion(msDocumentAssertion);
        assertions.add(documentResponseBodyAssertion);

        MsResponseBodyAssertion jsonPathResponseBodyAssertion = new MsResponseBodyAssertion();
        jsonPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.JSON_PATH.name());
        MsJSONPathAssertion msJSONPathAssertion = new MsJSONPathAssertion();
        MsJSONPathAssertionItem msJSONPathAssertionItem = new MsJSONPathAssertionItem();
        msJSONPathAssertionItem.setExpression("^test");
        msJSONPathAssertionItem.setCondition(MsAssertionCondition.REGEX.name());
        msJSONPathAssertionItem.setExpectedValue("expectedValue");
        msJSONPathAssertion.setAssertions(List.of(msJSONPathAssertionItem));
        jsonPathResponseBodyAssertion.setJsonPathAssertion(msJSONPathAssertion);
        assertions.add(jsonPathResponseBodyAssertion);

        MsResponseBodyAssertion xpathPathResponseBodyAssertion = new MsResponseBodyAssertion();
        xpathPathResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.XPATH.name());
        MsXPathAssertion xPathPathAssertion = new MsXPathAssertion();
        xPathPathAssertion.setResponseFormat(MsXPathAssertion.ResponseFormat.XML.name());
        MsXPathAssertionItem xPathAssertionItem = new MsXPathAssertionItem();
        xPathAssertionItem.setExpression("^test");
        xPathAssertionItem.setExpectedValue("expectedValue");
        xPathPathAssertion.setAssertions(List.of(xPathAssertionItem));
        xpathPathResponseBodyAssertion.setXpathAssertion(xPathPathAssertion);
        assertions.add(xpathPathResponseBodyAssertion);

        MsResponseTimeAssertion responseTimeAssertion = new MsResponseTimeAssertion();
        responseTimeAssertion.setExpectedValue(1000L);
        responseTimeAssertion.setEnable(true);
        responseTimeAssertion.setName("aa");
        assertions.add(responseTimeAssertion);

        MsScriptAssertion scriptAssertion = new MsScriptAssertion();
        scriptAssertion.setCommonScriptInfo(new CommonScriptInfo());
        scriptAssertion.getCommonScriptInfo().setId("1111");
        scriptAssertion.setScript("1111");
        scriptAssertion.setName("1111");
        assertions.add(scriptAssertion);

        MsVariableAssertion msVariableAssertion = new MsVariableAssertion();
        MsVariableAssertion.VariableAssertionItem variableAssertionItem = new MsVariableAssertion.VariableAssertionItem();
        variableAssertionItem.setCondition(MsAssertionCondition.GT.name());
        variableAssertionItem.setExpectedValue("ev");
        variableAssertionItem.setVariableName("vn");
        msVariableAssertion.setVariableAssertionItems(List.of(variableAssertionItem));
        assertions.add(msVariableAssertion);
        return assertions;
    }

    private MsEnvAssertionConfig createAssertionConfig() {
        MsEnvAssertionConfig assertions = new MsEnvAssertionConfig();
        assertions.setAssertions(getGeneralAssertions());
        return assertions;
    }


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAddSuccess() throws Exception {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("name");
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        Environment response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        Environment environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        EnvironmentBlob environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        String config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //公共参数
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setCommonParams(createCommonParams());
        request.setName("commonParams");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getCommonParams());
            Assertions.assertEquals(envConfig.getCommonParams(), environmentConfig.getCommonParams());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //公共变量
        envConfig.setCommonVariables(createCommonVariables(1));
        request.setName("commonVariables");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getCommonVariables());
            Assertions.assertEquals(envConfig.getCommonVariables(), environmentConfig.getCommonVariables());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //http配置
        envConfig.setHttpConfig(createHttpConfig());
        request.setName("httpConfig");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getHttpConfig());
            Assertions.assertEquals(envConfig.getHttpConfig(), environmentConfig.getHttpConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //数据源
        envConfig.setDataSources(createDataSources());
        request.setName("dataSources");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getDataSources());
            Assertions.assertEquals(envConfig.getDataSources(), environmentConfig.getDataSources());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //host配置
        envConfig.setHostConfig(crateHostConfig());
        request.setName("hostConfig");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getHostConfig());
            Assertions.assertEquals(envConfig.getHostConfig(), environmentConfig.getHostConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);
        //前置脚本
        envConfig.setPreProcessorConfig(createEnvironmentProcessorConfig());
        request.setName("preScript");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getPreProcessorConfig());
            Assertions.assertEquals(envConfig.getPreProcessorConfig(), environmentConfig.getPreProcessorConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //后置脚本
        envConfig.setPostProcessorConfig(createEnvironmentProcessorConfig());
        request.setName("postScript");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        Assertions.assertEquals(response.getId(), environment.getId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        Assertions.assertNotNull(config);
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getPostProcessorConfig());
            Assertions.assertEquals(envConfig.getPostProcessorConfig(), environmentConfig.getPostProcessorConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //断言
        envConfig.setAssertionConfig(createAssertionConfig());
        request.setName("assertions");
        request.setConfig(envConfig);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        Assertions.assertEquals(response.getProjectId(), environment.getProjectId());
        Assertions.assertEquals(response.getId(), environment.getId());
        environmentBlob = environmentBlobMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environmentBlob);
        config = new String(environmentBlob.getConfig());
        Assertions.assertNotNull(config);
        if (StringUtils.isNotBlank(config)) {
            EnvironmentConfig environmentConfig = JSON.parseObject(config, EnvironmentConfig.class);
            Assertions.assertNotNull(environmentConfig);
            Assertions.assertNotNull(environmentConfig.getAssertionConfig());
            Assertions.assertEquals(envConfig.getAssertionConfig(), environmentConfig.getAssertionConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //上传文件
        request.setName("uploadFile");
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/e5d6b195f54c96a59b3c0a9c8888798f.p12")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("e5d6b195f54c96a59b3c0a9c8888798f.p12", "e5d6b195f54c96a59b3c0a9c8888798f.p12", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MockMultipartFile file11 = new MockMultipartFile("file", "测试一下a", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());
        paramMap.add("file", List.of(file, file11));
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = requestMultipartWithOk(add, paramMap, DEFAULT_PROJECT_ID);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertEquals(response.getName(), environment.getName());

        //校验权限
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("校验权限");
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipartPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, add, paramMap);

    }

    @Test
    @Order(2)
    public void testAddError() throws Exception {
        //校验参数 项目id为空
        EnvironmentRequest request = new EnvironmentRequest();
        request.setProjectId(null);
        request.setName("name111");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, BAD_REQUEST_MATCHER);
        //名称为空
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName(null);
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, BAD_REQUEST_MATCHER);
        //名称重复
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(3)
    public void testGetSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.getFirst().getId();
        MvcResult mvcResult = this.responseGet(get + id);
        EnvironmentInfoDTO response = parseObjectFromMvcResult(mvcResult, EnvironmentInfoDTO.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(id, response.getId());
        Assertions.assertEquals("name", response.getName());
        //返回的配置信息为空
        Environment environment = new Environment();
        environment.setId("environmentId1");
        environment.setName("环境1");
        environment.setProjectId(DEFAULT_PROJECT_ID);
        environment.setUpdateUser("updateUser");
        environment.setUpdateTime(System.currentTimeMillis());
        environment.setCreateUser("createUser");
        environment.setMock(false);
        environment.setPos(1000L);
        environment.setCreateTime(System.currentTimeMillis());
        environmentMapper.insert(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId("environmentId1");
        environmentBlob.setConfig(JSON.toJSONBytes(new EnvironmentConfig()));
        environmentBlobMapper.insert(environmentBlob);
        mvcResult = this.responseGet(get + "environmentId1");
        response = parseObjectFromMvcResult(mvcResult, EnvironmentInfoDTO.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("environmentId1", response.getId());
        //插入一个为空的mock环境
        Environment mockEnvironment = new Environment();
        mockEnvironment.setId("mock-null");
        mockEnvironment.setMock(true);
        mockEnvironment.setProjectId(DEFAULT_PROJECT_ID);
        mockEnvironment.setUpdateUser("updateUser");
        mockEnvironment.setUpdateTime(System.currentTimeMillis());
        mockEnvironment.setCreateUser("createUser");
        mockEnvironment.setPos(1000L);
        mockEnvironment.setName("mock-null");
        mockEnvironment.setCreateTime(System.currentTimeMillis());
        environmentMapper.insert(mockEnvironment);
        this.responseGet(get + "mock-null");
        environmentMapper.deleteByPrimaryKey("mock-null");
        //校验权限
        requestGetPermissionTest(PROJECT_ENVIRONMENT_READ, get + DEFAULT_PROJECT_ID);
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environmentList = environmentMapper.selectByExample(environmentExample);
        MOCKID = environmentList.getFirst().getId();
        this.responseGet(get + MOCKID);
    }

    @Test
    @Order(4)
    public void testGetError() throws Exception {
        //环境不存在
        MvcResult mvcResult = this.responseGet(get + "environmentId2");
        EnvironmentRequest response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNull(response);

    }

    /**
     * 测试数据库链接
     */
    @Test
    @Order(5)
    public void testDataValidate() throws Exception {
        //测试mysql
        DataSource dataSource = new DataSource();
        dataSource.setDbUrl(dburl);
        dataSource.setPassword(password);
        dataSource.setUsername(username);
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        this.requestPost(validate, dataSource, status().isOk());
        //校验权限
        requestPostPermissionsTest(Arrays.asList(PROJECT_ENVIRONMENT_READ, PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE),
                validate, dataSource);
    }

    @Test
    @Order(6)
    public void testDataValidateError() throws Exception {
        //测试mysql dbUrl为空
        DataSource dataSource = new DataSource();
        dataSource.setDbUrl(null);
        dataSource.setPassword(password);
        dataSource.setUsername(username);
        //测试mysql DriverId为空
        dataSource.setDriverId(null);
        this.requestPost(validate, dataSource, BAD_REQUEST_MATCHER);
        // 测试500
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setDbUrl("jdbc:mysql://");
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        this.requestPost(validate, dataSource, ERROR_REQUEST_MATCHER);
        dataSource.setDbUrl(dburl);
        dataSource.setUsername("测试数据源");
        dataSource.setPassword("测试数据源");
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        this.requestPost(validate, dataSource, ERROR_REQUEST_MATCHER);
        dataSource.setDbUrl("测试数据源");
        dataSource.setUsername("测试数据源");
        dataSource.setPassword("测试数据源");
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        this.requestPost(validate, dataSource, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(7)
    public void testGetDriverOptions() throws Exception {
        MvcResult mvcResult = this.responseGet(getOptions + DEFAULT_ORGANIZATION_ID);
        List<OptionDTO> optionDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(optionDTOS);

        //校验权限
        requestGetPermissionTest(PROJECT_ENVIRONMENT_READ, getOptions + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(8)
    public void testUpdateSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.getFirst().getId();
        EnvironmentRequest request = new EnvironmentRequest();
        request.setId(id);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        MvcResult mvcResult = requestMultipartWithOkAndReturn(update, paramMap, ERROR_REQUEST_MATCHER);
        Environment response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        Environment environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        request.setName("test-edit-name");
        request.setConfig(null);
        paramMap.clear();
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = requestMultipartWithOkAndReturn(update, paramMap);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());

        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("commonParams");
        environments = environmentMapper.selectByExample(example);
        request.setId(environments.getFirst().getId());
        request.setName("commonParams");
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/e5d6b195f54c96a59b3c0a9c8888798f.p12")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("e5d6b195f54c96a59b3c0a9c8888798f.p12", "e5d6b195f54c96a59b3c0a9c8888798f.p12", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MockMultipartFile file11 = new MockMultipartFile("file", "测试一下a", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());
        paramMap.add("file", List.of(file, file11));
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = requestMultipartWithOk(update, paramMap, DEFAULT_PROJECT_ID);
        response = parseObjectFromMvcResult(mvcResult, Environment.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        //校验权限
        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("校验权限");
        environments = environmentMapper.selectByExample(example);
        id = environments.getFirst().getId();
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("校验更新权限");
        request.setId(id);
        request.setConfig(new EnvironmentConfig());
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipartPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE, update, paramMap);
    }

    @Test
    @Order(9)
    public void testUpdateError() throws Exception {
        //校验环境不存在
        EnvironmentRequest request = new EnvironmentRequest();
        request.setId("environmentId2");
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("test-edit-name");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(update, paramMap, ERROR_REQUEST_MATCHER);
        //重名
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("test-edit-name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.getFirst().getId();
        request = new EnvironmentRequest();
        request.setId(id);
        request.setProjectId(DEFAULT_PROJECT_ID);
        request.setName("uploadFile");
        request.setConfig(new EnvironmentConfig());
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(update, paramMap, ERROR_REQUEST_MATCHER);

    }

    @Test
    @Order(10)
    public void testPos() throws Exception {
        PosRequest posRequest = new PosRequest();
        posRequest.setProjectId(DEFAULT_PROJECT_ID);
        posRequest.setTargetId("environmentId1");
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(example);
        posRequest.setMoveId(environments.getFirst().getId());
        posRequest.setMoveMode("AFTER");
        this.requestPostWithOkAndReturn(POS_URL, posRequest, DEFAULT_PROJECT_ID);

        posRequest.setMoveMode("BEFORE");
        this.requestPostWithOkAndReturn(POS_URL, posRequest, DEFAULT_PROJECT_ID);
        //extEnvironmentMapper
        DropNode environmentId1 = extEnvironmentMapper.selectDragInfoById("environmentId1");

        NodeSortQueryParam sortParam = new NodeSortQueryParam();
        sortParam.setPos(environmentId1.getPos());
        sortParam.setOperator("moreThan");
        sortParam.setParentId(DEFAULT_PROJECT_ID);

        DropNode dropNode = extEnvironmentMapper.selectNodeByPosOperator(sortParam);
        extEnvironmentMapper.updatePos(dropNode.getId(), 999);
        Assertions.assertNotNull(dropNode);
        environments = environmentMapper.selectByExample(example);
        posRequest.setMoveMode("AFTER");
        posRequest.setMoveId(environments.getFirst().getId());
        this.requestPostWithOkAndReturn(POS_URL, posRequest, DEFAULT_PROJECT_ID);

    }

    @Test
    @Order(11)
    public void testDeleteSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("test-edit-name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.getFirst().getId();
        this.requestGet(delete + id);
        //校验日志
        checkLog(id, OperationLogType.DELETE);
        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE, delete + id);

        //删除环境不存在的
        this.requestGet(delete + "environmentId2", ERROR_REQUEST_MATCHER);

        this.requestGet(delete + MOCKID, ERROR_REQUEST_MATCHER);

        //删除包含文件的环境
        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("uploadFile");
        environments = environmentMapper.selectByExample(example);
        id = environments.getFirst().getId();
        this.requestGet(delete + id);
        //查询文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFolder(DefaultRepositoryDir.getEnvSslDir(DEFAULT_PROJECT_ID, id));
        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);
        assert minioRepository != null;
        List<String> fileNames = minioRepository.getFolderFileNames(fileRequest);
        //校验文件
        Assertions.assertEquals(0, fileNames.size());
        //校验日志
        checkLog(id, OperationLogType.DELETE);
    }

    @Test
    @Order(12)
    public void testList() throws Exception {
        EnvironmentFilterRequest environmentDTO = new EnvironmentFilterRequest();
        environmentDTO.setProjectId(DEFAULT_PROJECT_ID);
        MvcResult mvcResult = this.responsePost(list, environmentDTO);
        List<Environment> response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);
        //输入搜索值
        environmentDTO.setKeyword("commonParams");
        mvcResult = this.responsePost(list, environmentDTO);
        response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);
        //校验拿到的数据包含搜索值
        Assertions.assertEquals(1, response.size());
        environmentDTO.setProjectId(DEFAULT_PROJECT_ID);
        //校验权限
        requestPostPermissionTest(PROJECT_ENVIRONMENT_READ, list, environmentDTO);

        //项目不存在 返回内容为[]
        environmentDTO.setProjectId("ceshi");
        mvcResult = this.responsePost(list, environmentDTO);
        response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertEquals(0, response.size());
    }

    @Test
    @Order(13)
    public void testGetEntry() throws Exception {
        String password = "123456";
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/2538ee56c127c56ab3a8dabb4f685067.jks")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("file", "2538ee56c127c56ab3a8dabb4f685067.jks", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        paramMap.set("request", password);
        requestMultipartWithOk(getEnTry, paramMap, DEFAULT_PROJECT_ID);


        password = "123456789";
        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/2538ee56c127c56ab3a8dabb4f685067.jks")
                        .getPath()));

        file = new MockMultipartFile("file", "2538ee56c127c56ab3a8dabb4f685067.jks", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        paramMap.set("request", password);
        requestMultipartWithOkAndReturn(getEnTry, paramMap, ERROR_REQUEST_MATCHER);

        //文件名称有问题
        password = "123456";

        file = new MockMultipartFile("file", "ceshi/aa.jks", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());
        paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        paramMap.set("request", password);
        requestMultipartWithOkAndReturn(getEnTry, paramMap, ERROR_REQUEST_MATCHER);


    }

    @Test
    @Order(14)
    public void testExport() throws Exception {
        //指定id
        TableBatchProcessDTO request = new TableBatchProcessDTO();
        request.setSelectIds(List.of("environmentId1"));
        MvcResult mvcResult = this.requestPostDownloadFile(exportEnv, null, request, DEFAULT_PROJECT_ID);
        byte[] fileBytes = mvcResult.getResponse().getContentAsByteArray();
        Assertions.assertNotNull(fileBytes);

        request.setSelectIds(List.of("environmentId1"));
        request.setSelectAll(true);
        request.setExcludeIds(List.of("environmentId1"));
        MvcResult mvcResult1 = this.requestPostDownloadFile(exportEnv, null, request, DEFAULT_PROJECT_ID);
        byte[] fileBytes1 = mvcResult1.getResponse().getContentAsByteArray();
        Assertions.assertNotNull(fileBytes1);
        request.setSelectIds(List.of("不存在blob"));
        request.setSelectAll(false);
        mockMvc.perform(getPostRequestBuilder(exportEnv, request, DEFAULT_PROJECT_ID))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(ERROR_REQUEST_MATCHER);

    }

    protected MvcResult requestPostDownloadFile(String url, MediaType contentType, Object param, String projectId) throws Exception {
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return mockMvc.perform(getPostRequestBuilder(url, param, projectId))
                .andExpect(content().contentType(contentType))
                .andExpect(status().isOk()).andReturn();
    }

    protected MockHttpServletRequestBuilder getPostRequestBuilder(String url, Object param, Object... uriVariables) {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(getBasePath() + url);
        return requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(SessionConstants.CURRENT_PROJECT, uriVariables)
                .header(org.apache.http.HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .content(JSON.toJSONString(param))
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    @Order(15)
    public void testImport() throws Exception {
        //校验参数
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/huanj.json")
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file", "huanj.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        EnvironmentImportRequest request = new EnvironmentImportRequest();
        request.setCover(true);
        paramMap.add("file", List.of(file));
        paramMap.add("request", JSON.toJSONString(request));
        requestMultipartWithOk(importEnv, paramMap, DEFAULT_PROJECT_ID);

        requestMultipartWithOk(importEnv, paramMap, DEFAULT_PROJECT_ID);
        request.setCover(false);
        paramMap.clear();
        paramMap.add("file", List.of(file));
        paramMap.add("request", JSON.toJSONString(request));
        requestMultipartWithOk(importEnv, paramMap, DEFAULT_PROJECT_ID);

        inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/txtFile.txt")
                        .getPath()));
        file = new MockMultipartFile("file", "txtFile.txt", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        paramMap = new LinkedMultiValueMap<>();
        request = new EnvironmentImportRequest();
        request.setCover(true);
        paramMap.add("file", List.of(file));
        paramMap.add("request", JSON.toJSONString(request));
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(importEnv, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header(SessionConstants.CURRENT_PROJECT, DEFAULT_PROJECT_ID);
        mockMvc.perform(header)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(16)
    public void testScripts() throws Exception {
        Plugin plugin = addEnvScriptsTestPlugin();

        MvcResult mvcResult = requestGetWithOkAndReturn(SCRIPTS, DEFAULT_PROJECT_ID);
        List<EnvironmentPluginScriptDTO> envScripts = getResultDataArray(mvcResult, EnvironmentPluginScriptDTO.class);
        Assertions.assertEquals(envScripts.size(), 1);
        Assertions.assertEquals(envScripts.getFirst().getPluginId(), "tcp-sampler");
        Assertions.assertEquals(((Map) envScripts.getFirst().getScript()).get("id"), "environment");

        // 删除环境脚本，测试是否正常执行
        pluginScriptMapper.deleteByPrimaryKey(plugin.getId(), "environment");
        mvcResult = requestGetWithOkAndReturn(SCRIPTS, DEFAULT_PROJECT_ID);
        envScripts = getResultDataArray(mvcResult, EnvironmentPluginScriptDTO.class);
        Assertions.assertEquals(envScripts.size(), 0);

        pluginService.delete(plugin.getId());
        requestGetPermissionTest(PROJECT_ENVIRONMENT_READ, SCRIPTS, DEFAULT_PROJECT_ID);
    }

    public Plugin addEnvScriptsTestPlugin() throws Exception {
        PluginUpdateRequest request = new PluginUpdateRequest();
        File jarFile = new File(
                this.getClass().getClassLoader().getResource("file/tcp-sampler-v3.x-jar-with-dependencies.jar")
                        .getPath()
        );
        FileInputStream inputStream = new FileInputStream(jarFile);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(jarFile.getName(), jarFile.getName(), "jar", inputStream);
        request.setName("测试获取环境脚本插件");
        request.setGlobal(true);
        request.setEnable(true);
        request.setCreateUser(ADMIN.name());
        return pluginService.add(request, mockMultipartFile);
    }

    @Test
    @Order(17)
    public void testGetOptions() throws Exception {
        MvcResult mvcResult = responseGet(listOption + DEFAULT_PROJECT_ID);
        List<EnvironmentOptionsDTO> options = getResultDataArray(mvcResult, EnvironmentOptionsDTO.class);
        Assertions.assertFalse(options.isEmpty());
    }

    @Test
    @Order(18)
    public void addCover() throws Exception {
        List<Environment> environments = environmentMapper.selectByExample(new EnvironmentExample());
        //获取id集合 过滤一下mock为false的
        List<String> ids = environments.stream().map(Environment::getId).toList();
        environmentService.getByIds(ids);
        environmentService.getEnvironmentBlobsByIds(List.of());
    }
}
