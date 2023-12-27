package io.metersphere.system.controller;

import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BasePluginTestService;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.Plugin;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserPlatformAccountControllerTests extends BaseTest {

    @Resource
    private BasePluginTestService basePluginTestService;
    @Value("${embedded.mockserver.host}")
    private String mockServerHost;
    @Value("${embedded.mockserver.port}")
    private int mockServerHostPort;
    @Resource
    private MockServerClient mockServerClient;
    private static final String VALIDATE_POST = "/user/platform/validate/{0}";
    private static final String SAVE_POST = "/user/platform/save";
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

    @Test
    @Order(1)
    public void testGetAccountInfoList() throws Exception {
        basePluginTestService.getJiraPlugin();
        MvcResult mvcResult = this.requestGetAndReturn("/user/platform/account/info");
        Map<String, Object> accountMap = parseObjectFromMvcResult(mvcResult, Map.class);
        Assertions.assertNotNull(accountMap);
    }

    @Test
    @Order(2)
    public void validatePost() throws Exception {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/rest/api/2/myself"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeaders(
                                        new Header("Content-Type", "application/json; charset=utf-8"),
                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{\"self\"")
                );
        Plugin plugin = basePluginTestService.getJiraPlugin();
        BasePluginTestService.JiraIntegrationConfig integrationConfig = new BasePluginTestService.JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> integrationConfigMap = JSON.parseMap(JSON.toJSONString(integrationConfig));
        // @@请求成功
        this.requestPostWithOk(VALIDATE_POST, integrationConfigMap, plugin.getId());
    }

    @Test
    @Order(3)
    public void testSave() throws Exception {
        this.requestGetAndReturn("/user/platform/get");
        basePluginTestService.getJiraPlugin();
        BasePluginTestService.JiraIntegrationConfig integrationConfig = new BasePluginTestService.JiraIntegrationConfig();
        integrationConfig.setAddress(String.format("http://%s:%s", mockServerHost, mockServerHostPort));
        Map<String, Object> jiraMap = new HashMap<>();
        jiraMap.put("jira", integrationConfig);
        // @@请求成功
        this.requestPostWithOk(SAVE_POST, jiraMap);
        this.requestPostWithOk(SAVE_POST, jiraMap);
    }

    @Test
    @Order(4)
    public void testGet() throws Exception {
        MvcResult mvcResult = this.requestGetAndReturn("/user/platform/get");
        Map<String, Object> accountMap = parseObjectFromMvcResult(mvcResult, Map.class);
        Assertions.assertNotNull(accountMap);
    }





}
