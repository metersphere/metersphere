package io.metersphere.project.controller;


import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentRequest;
import io.metersphere.project.dto.environment.KeyValue;
import io.metersphere.project.dto.environment.assertions.*;
import io.metersphere.project.dto.environment.auth.AuthConfig;
import io.metersphere.project.dto.environment.common.CommonParams;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.dto.environment.host.Host;
import io.metersphere.project.dto.environment.host.HostConfig;
import io.metersphere.project.dto.environment.http.ApplicationModule;
import io.metersphere.project.dto.environment.http.HttpConfig;
import io.metersphere.project.dto.environment.script.ScriptContent;
import io.metersphere.project.dto.environment.script.post.EnvironmentPostScript;
import io.metersphere.project.dto.environment.script.post.UiPostScript;
import io.metersphere.project.dto.environment.script.pre.EnvironmentPreScript;
import io.metersphere.project.dto.environment.script.pre.UiPreScript;
import io.metersphere.project.dto.environment.ssl.KeyStoreConfig;
import io.metersphere.project.dto.environment.ssl.KeyStoreEntry;
import io.metersphere.project.dto.environment.ssl.KeyStoreFile;
import io.metersphere.project.dto.environment.tcp.TCPConfig;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentBlob;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.file.MinioRepository;
import io.metersphere.sdk.mapper.EnvironmentBlobMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.log.constants.OperationLogType;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnvironmentControllerTests extends BaseTest {

    private static final String prefix = "/project/environment";
    private static final String add = prefix + "/add";
    private static final String get = prefix + "/get/";
    private static final String update = prefix + "/update";
    private static final String delete = prefix + "/delete/";
    private static final String list = prefix + "/list/";
    private static final String getEnTry = prefix + "/get/entry";
    private static final String importEnv = prefix + "/import";
    private static final String exportEnv = prefix + "/export";

    private static final String validate = prefix + "/database/validate";
    private static final String getOptions = prefix + "/database/driver-options/";
    private static final ResultMatcher BAD_REQUEST_MATCHER = status().isBadRequest();
    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();
    private static final String DIR_PATH = "/project-management/environment/";
    @Resource
    private MockMvc mockMvc;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private EnvironmentBlobMapper environmentBlobMapper;
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

    protected MvcResult requestMultipartWithOk(String url, MultiValueMap<String, Object> paramMap) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = getMultipartRequestBuilderWithParam(url, paramMap);
        MockHttpServletRequestBuilder header = requestBuilder
                .header(SessionConstants.HEADER_TOKEN, sessionId)
                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .header("PROJECT", "projectId");
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
        List<KeyValue> headers = new ArrayList<>();
        KeyValue keyValue = new KeyValue();
        keyValue.setName("key");
        keyValue.setValue("value");
        headers.add(keyValue);
        httpConfig.setHeaders(headers);
        httpConfig.setDomain("domain");
        httpConfig.setProtocol("https");
        httpConfig.setSocket("socket");

        httpConfigs.add(httpConfig);
        return httpConfigs;
    }


    private List<CommonVariables> createCommonVariables(int count) {
        List<CommonVariables> commonVariables = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CommonVariables commonVariable = new CommonVariables();
            commonVariable.setName("key" + i);
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
        host.setUuid("uuid");
        host.setDomain("domain");
        host.setDescription("description");
        host.setIp("ip");
        hostConfig.setHosts(List.of(host));
        return hostConfig;
    }

    private TCPConfig createTCPConfig() {
        TCPConfig tcpConfig = new TCPConfig();
        tcpConfig.setClassName("className");
        tcpConfig.setPort(8080);
        tcpConfig.setServer("server");
        tcpConfig.setPassword("password");
        tcpConfig.setUsername("username");
        tcpConfig.setConnectTimeout("connectTimeout");
        tcpConfig.setTimeout("timeout");
        tcpConfig.setSoLinger("soLinger");
        tcpConfig.setReUseConnection(true);
        tcpConfig.setNodelay(true);
        tcpConfig.setCloseConnection(true);
        tcpConfig.setEolByte("eolByte");

        return tcpConfig;
    }

    private AuthConfig createAuthConfig() {
        AuthConfig authConfig = new AuthConfig();
        authConfig.setUsername("username");
        authConfig.setPassword("password");
        return authConfig;
    }

    private EnvironmentPreScript createEnvironmentPreScript() {
        EnvironmentPreScript environmentPreScript = new EnvironmentPreScript();
        environmentPreScript.setApiPreScript(createApiScript());
        environmentPreScript.setUiPreScript(createUiPreScript());
        return environmentPreScript;
    }

    private EnvironmentPostScript createEnvironmentPostScript() {
        EnvironmentPostScript environmentPostScript = new EnvironmentPostScript();
        environmentPostScript.setApiPostScript(createApiScript());
        environmentPostScript.setUiPostScript(createUiPostScript());
        return environmentPostScript;
    }

    private KeyStoreConfig createKeyStoreConfig() {
        KeyStoreConfig keyStoreConfig = new KeyStoreConfig();
        KeyStoreEntry keyStoreEntry = new KeyStoreEntry();
        keyStoreEntry.setId("id");
        keyStoreEntry.setPassword("alias");
        keyStoreEntry.setDefault(true);
        keyStoreEntry.setSourceId("sourceId");
        keyStoreEntry.setSourceName("sourceName");
        keyStoreConfig.setEntry(List.of(keyStoreEntry));
        KeyStoreFile keyStoreFile = new KeyStoreFile();
        keyStoreFile.setId("id");
        keyStoreFile.setName("name");
        keyStoreFile.setType("type");
        keyStoreFile.setUpdateTime("updateTime");
        keyStoreFile.setPassword("password");
        keyStoreFile.setFile(null);
        keyStoreConfig.setFiles(List.of(keyStoreFile));
        return keyStoreConfig;
    }

    private ScriptContent.ApiScript createApiScript() {
        ScriptContent.ApiScript apiScript = new ScriptContent.ApiScript();
        ScriptContent scriptContent = new ScriptContent();
        scriptContent.setScript("script");
        apiScript.setEnvJSR223PostScript(scriptContent);
        ScriptContent.ScenarioPostScript scenarioPostScript = new ScriptContent.ScenarioPostScript();
        scenarioPostScript.setAssociateScenarioResults(true);
        scenarioPostScript.setScript("scenarioPostScript");
        apiScript.setScenarioJSR223PostScript(scenarioPostScript);
        ScriptContent.StepPostScript stepPostScript = new ScriptContent.StepPostScript();
        stepPostScript.setPreScriptExecBefore(true);
        stepPostScript.setScriptContent(scriptContent);
        stepPostScript.setFilterRequestPostScript(new ArrayList<>());
        apiScript.setStepJSR223PostScript(stepPostScript);
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

    private UiPostScript createUiPostScript() {
        UiPostScript uiPostScript = new UiPostScript();
        uiPostScript.setPostScriptExecBefore(true);
        uiPostScript.setJsrType(true);
        uiPostScript.setJsrSetVariable(true);
        uiPostScript.setVariableName("variableName");
        uiPostScript.setValue(true);
        return uiPostScript;
    }

    private EnvironmentAssertions createAssertions() {
        EnvironmentAssertions assertions = new EnvironmentAssertions();
        ApplicationModule applicationModule = new ApplicationModule();
        applicationModule.setApiTest(true);
        assertions.setModule(applicationModule);
        List<EnvAssertionRegex> regex = new ArrayList<>();
        assertions.setRegex(regex);
        List<EnvAssertionJsonPath> jsonPath = new ArrayList<>();
        assertions.setJsonPath(jsonPath);
        List<EnvAssertionJSR223> jsr223 = new ArrayList<>();
        assertions.setJsr223(jsr223);
        List<EnvAssertionXPath> xpath = new ArrayList<>();
        assertions.setXpath(xpath);
        return assertions;
    }


    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testAddSuccess() throws Exception {
        EnvironmentRequest request = new EnvironmentRequest();
        request.setProjectId("projectId");
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("request", JSON.toJSONString(request));
        MvcResult mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        EnvironmentRequest response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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

        //tcp配置
        envConfig.setTcpConfig(createTCPConfig());
        request.setName("tcpConfig");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getTcpConfig());
            Assertions.assertEquals(envConfig.getTcpConfig(), environmentConfig.getTcpConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //host配置
        envConfig.setHostConfig(crateHostConfig());
        request.setName("hostConfig");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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

        //auth配置
        envConfig.setAuthConfig(createAuthConfig());
        request.setName("authConfig");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getAuthConfig());
            Assertions.assertEquals(envConfig.getAuthConfig(), environmentConfig.getAuthConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //ssl配置
        envConfig.setSslConfig(createKeyStoreConfig());
        request.setName("sslConfig");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getSslConfig());
            Assertions.assertEquals(envConfig.getSslConfig(), environmentConfig.getSslConfig());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //前置脚本
        envConfig.setPreScript(createEnvironmentPreScript());
        request.setName("preScript");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getPreScript());
            Assertions.assertEquals(envConfig.getPreScript(), environmentConfig.getPreScript());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //后置脚本
        envConfig.setPostScript(createEnvironmentPostScript());
        request.setName("postScript");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getPostScript());
            Assertions.assertEquals(envConfig.getPostScript(), environmentConfig.getPostScript());
        }
        //校验日志
        checkLog(response.getId(), OperationLogType.ADD);

        //断言
        envConfig.setAssertions(createAssertions());
        request.setName("assertions");
        request.setConfig(envConfig);
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = this.requestMultipartWithOkAndReturn(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
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
            Assertions.assertNotNull(environmentConfig.getAssertions());
            Assertions.assertEquals(envConfig.getAssertions(), environmentConfig.getAssertions());
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
        mvcResult = requestMultipartWithOk(add, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        //查询文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setProjectId(StringUtils.join(DIR_PATH, environment.getProjectId()));
        fileRequest.setResourceId(environment.getId());
        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);
        assert minioRepository != null;
        List<String> fileNames = minioRepository.getFolderFileNames(fileRequest);
        //校验文件
        Assertions.assertEquals(2, fileNames.size());

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
        request.setProjectId("projectId");
        request.setName(null);
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, BAD_REQUEST_MATCHER);
        //名称重复
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, ERROR_REQUEST_MATCHER);
        //配置为空
        request.setConfig(null);
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(add, paramMap, BAD_REQUEST_MATCHER);


    }

    @Test
    @Order(3)
    public void testGetSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.get(0).getId();
        MvcResult mvcResult = this.responseGet(get + id);
        EnvironmentRequest response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals(id, response.getId());
        Assertions.assertEquals("name", response.getName());
        //返回的配置信息为空
        Environment environment = new Environment();
        environment.setId("environmentId1");
        environment.setName("环境1");
        environment.setProjectId("projectId");
        environment.setUpdateUser("updateUser");
        environment.setUpdateTime(System.currentTimeMillis());
        environment.setCreateUser("createUser");
        environment.setCreateTime(System.currentTimeMillis());
        environmentMapper.insert(environment);
        EnvironmentBlob environmentBlob = new EnvironmentBlob();
        environmentBlob.setId("environmentId1");
        environmentBlob.setConfig(JSON.toJSONBytes(new EnvironmentConfig()));
        environmentBlobMapper.insert(environmentBlob);
        mvcResult = this.responseGet(get + "environmentId1");
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("environmentId1", response.getId());
        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, get + "projectId");
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
        requestPostPermissionsTest(Arrays.asList(PermissionConstants.PROJECT_ENVIRONMENT_READ, PermissionConstants.PROJECT_ENVIRONMENT_READ_ADD, PermissionConstants.PROJECT_ENVIRONMENT_READ_UPDATE),
                validate, dataSource);
    }

    @Test
    @Order(6)
    public void testDataValidateError() throws Exception {
        //测试mysql dbUrl为空
        DataSource dataSource = new DataSource();
        dataSource.setDbUrl(null);
        dataSource.setPassword("Password123@mysql");
        dataSource.setUsername("root");
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        this.requestPost(validate, dataSource, BAD_REQUEST_MATCHER);
        //测试mysql DriverId为空
        dataSource.setDbUrl("jdbc:mysql://");
        dataSource.setDriverId(null);
        this.requestPost(validate, dataSource, BAD_REQUEST_MATCHER);
        //测试mysql Driver为空
        dataSource.setDriverId(StringUtils.join("system", "&", "com.mysql.cj.jdbc.Driver"));
        dataSource.setDriver(null);
        this.requestPost(validate, dataSource, BAD_REQUEST_MATCHER);
        // 测试500
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setDbUrl("jdbc:mysql://");
        this.requestPost(validate, dataSource, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(7)
    public void testGetDriverOptions() throws Exception {
        MvcResult mvcResult = this.responseGet(getOptions + DEFAULT_ORGANIZATION_ID);
        List<OptionDTO> optionDTOS = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(optionDTOS);

        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, getOptions + DEFAULT_PROJECT_ID);
    }

    @Test
    @Order(8)
    public void testUpdateSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.get(0).getId();
        EnvironmentRequest request = new EnvironmentRequest();
        request.setId(id);
        request.setProjectId("projectId");
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        MvcResult mvcResult = requestMultipartWithOkAndReturn(update, paramMap, ERROR_REQUEST_MATCHER);
        EnvironmentRequest response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);
        Environment environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertNotNull(environment);
        Assertions.assertEquals(response.getId(), environment.getId());
        Assertions.assertEquals(response.getName(), environment.getName());

        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("commonParams");
        environments = environmentMapper.selectByExample(example);
        request.setId(environments.get(0).getId());
        request.setName("commonParams");
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/e5d6b195f54c96a59b3c0a9c8888798f.p12")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("e5d6b195f54c96a59b3c0a9c8888798f.p12", "e5d6b195f54c96a59b3c0a9c8888798f.p12", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MockMultipartFile file11 = new MockMultipartFile("file", "测试一下a", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());
        paramMap.add("file", List.of(file, file11));
        paramMap.set("request", JSON.toJSONString(request));
        mvcResult = requestMultipartWithOk(update, paramMap);
        response = parseObjectFromMvcResult(mvcResult, EnvironmentRequest.class);
        Assertions.assertNotNull(response);
        environment = environmentMapper.selectByPrimaryKey(response.getId());
        Assertions.assertEquals(response.getName(), environment.getName());
        //查询文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setProjectId(StringUtils.join(DIR_PATH, environment.getProjectId()));
        fileRequest.setResourceId(environment.getId());
        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);
        assert minioRepository != null;
        List<String> fileNames = minioRepository.getFolderFileNames(fileRequest);
        //校验文件
        Assertions.assertEquals(2, fileNames.size());
        //校验权限
        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID).andNameEqualTo("校验权限");
        environments = environmentMapper.selectByExample(example);
        id = environments.get(0).getId();
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
        request.setProjectId("projectId");
        request.setName("name");
        request.setConfig(new EnvironmentConfig());
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(update, paramMap, ERROR_REQUEST_MATCHER);
        //重名
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.get(0).getId();
        request = new EnvironmentRequest();
        request.setId(id);
        request.setProjectId("projectId");
        request.setName("uploadFile");
        request.setConfig(new EnvironmentConfig());
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(update, paramMap, ERROR_REQUEST_MATCHER);
        //配置为空
        request = new EnvironmentRequest();
        request.setId("environmentId2");
        request.setProjectId("projectId");
        request.setName("name");
        request.setConfig(null);
        paramMap = new LinkedMultiValueMap<>();
        paramMap.set("request", JSON.toJSONString(request));
        requestMultipart(update, paramMap, BAD_REQUEST_MATCHER);

    }

    @Test
    @Order(10)
    public void testDeleteSuccess() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("name");
        List<Environment> environments = environmentMapper.selectByExample(example);
        String id = environments.get(0).getId();
        this.requestGet(delete + id);
        //校验日志
        checkLog(id, OperationLogType.DELETE);
        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ_DELETE, delete + id);

        //删除环境不存在的
        this.requestGet(delete + "environmentId2", ERROR_REQUEST_MATCHER);

        //删除包含文件的环境
        example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId").andNameEqualTo("uploadFile");
        environments = environmentMapper.selectByExample(example);
        id = environments.get(0).getId();
        this.requestGet(delete + id);
        //查询文件
        FileRequest fileRequest = new FileRequest();
        fileRequest.setProjectId(StringUtils.join(DIR_PATH, "projectId"));
        fileRequest.setResourceId(id);
        MinioRepository minioRepository = CommonBeanFactory.getBean(MinioRepository.class);
        assert minioRepository != null;
        List<String> fileNames = minioRepository.getFolderFileNames(fileRequest);
        //校验文件
        Assertions.assertEquals(0, fileNames.size());
        //校验日志
        checkLog(id, OperationLogType.DELETE);
    }

    @Test
    @Order(11)
    public void testList() throws Exception {
        MvcResult mvcResult = this.responseGet(list + "projectId");
        List<Environment> response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);

        //校验权限
        requestGetPermissionTest(PermissionConstants.PROJECT_ENVIRONMENT_READ, list + DEFAULT_PROJECT_ID);

        //项目不存在 返回内容为[]
        mvcResult = this.responseGet(list + "ceshi");
        response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertEquals(0, response.size());
    }

    @Test
    @Order(12)
    public void testGetEntry() throws Exception {
        String password = "123456";
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/2538ee56c127c56ab3a8dabb4f685067.jks")
                        .getPath()));

        MockMultipartFile file = new MockMultipartFile("file", "2538ee56c127c56ab3a8dabb4f685067.jks", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        paramMap.set("request", password);
        MvcResult mvcResult = requestMultipartWithOk(getEnTry, paramMap);
        List<KeyStoreEntry> response = parseObjectFromMvcResult(mvcResult, List.class);
        Assertions.assertNotNull(response);

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
    @Order(13)
    public void testExport() throws Exception {
        //校验参数
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo("projectId");
        List<Environment> environments = environmentMapper.selectByExample(example);
        //把环境的id整理为一个集合
        List<String> ids = environments.stream().map(Environment::getId).toList();

        MvcResult mvcResult = this.responsePost(exportEnv, ids);
        String response = parseObjectFromMvcResult(mvcResult, String.class);
        Assertions.assertNotNull(response);

        //传id为空
        mvcResult = this.responsePost(exportEnv, new ArrayList<>());
        response = parseObjectFromMvcResult(mvcResult, String.class);
        Assertions.assertNull(response);
    }

    @Test
    @Order(14)
    public void testImport() throws Exception {
        //校验参数
        FileInputStream inputStream = new FileInputStream(new File(
                this.getClass().getClassLoader().getResource("file/huanj.json")
                        .getPath()));
        MockMultipartFile file = new MockMultipartFile("file", "huanj.json", MediaType.APPLICATION_OCTET_STREAM_VALUE, inputStream);
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("file", List.of(file));
        requestMultipartWithOk(importEnv, paramMap);

    }
}
