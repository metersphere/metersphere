package io.metersphere.api.controller;

import io.metersphere.api.engine.ApiEngine;
import io.metersphere.api.engine.EngineFactory;
import io.metersphere.sdk.constants.ResourcePoolTypeEnum;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.dto.api.task.TaskRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.pool.TestResourceDTO;
import io.metersphere.system.dto.pool.TestResourcePoolRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class KubernetesEngineTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;
    private static final String TEST_RESOURCE_POOL_ADD = "/test/resource/pool/add";
    private static final String configurationWidthOutOrgIds = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\":[{\n" +
            "       \"ip\":\"192.168.20.17\",\n" +
            "       \"port\": \"1194\",\n" +
            "       \"monitor\": \"9100\",\n" +
            "       \"concurrentNumber\": 100\n" +
            "   }],\n" +
            "\"ip\":\"172.2.130.1\",\n" +
            "\"token\":\"dsdfssdsvgsd\",\n" +
            "\"namespace\":\"测试\",\n" +
            "\"concurrentNumber\":3,\n" +
            "\"podThreads\":2,\n" +
            "\"jobDefinition\":\"jsfsjs\",\n" +
            "\"deployName\":\"hello\",\n" +
            "\"uiGrid\":\"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";

    private static final String configuration = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\": [\n" +
            "    {\n" +
            "      \"ip\": \"172.16.200.8\",\n" +
            "      \"port\": \"8082\",\n" +
            "      \"monitor\": \"9100\",\n" +
            "      \"concurrentNumber\": 100\n" +
            "    }\n" +
            "  ],\n" +
            "  \"orgIds\": [\"sys_default_organization_2\",\"sys_default_organization_3\"],\n" +
            "  \"ip\": \"172.2.130.1\",\n" +
            "  \"token\": \"test\",\n" +
            "  \"namespace\": \"test\",\n" +
            "  \"concurrentNumber\": 3,\n" +
            "  \"podThreads\": 2,\n" +
            "  \"jobDefinition\": \"jsfsjs\",\n" +
            "  \"deployName\": \"hello\",\n" +
            "  \"uiGrid\": \"localhost:4444\",\n" +
            "\"girdConcurrentNumber\":2\n" +
            "}";

    @Value("${embedded.mockserver.host}")
    private String host;
    @Value("${embedded.mockserver.port}")
    private int port;

    private void setResources(TestResourcePoolRequest testResourcePoolDTO, boolean isPart) {
        TestResourceDTO testResourceDTO;
        if (isPart) {
            testResourceDTO = JSON.parseObject(configuration, TestResourceDTO.class);
            testResourceDTO.getNodesList().forEach(testResourceNodeDTO -> {
                testResourceNodeDTO.setIp(host);
                testResourceNodeDTO.setPort(port + "");
            });
        } else {
            testResourceDTO = JSON.parseObject(configurationWidthOutOrgIds, TestResourceDTO.class);
        }
        testResourcePoolDTO.setTestResourceDTO(testResourceDTO);
    }

    private static TestResourcePool getResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        String contentAsString = mvcResult.getResponse().getContentAsString();
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), TestResourcePool.class);
    }

    private String addPool(String type) throws Exception {
        TestResourcePoolRequest testResourcePoolRequest = new TestResourcePoolRequest();
        testResourcePoolRequest.setId("");
        testResourcePoolRequest.setName("api_test_pool");
        testResourcePoolRequest.setType(type);
        testResourcePoolRequest.setApiTest(false);
        testResourcePoolRequest.setLoadTest(false);
        testResourcePoolRequest.setUiTest(false);
        //添加成功 需要加应用组织的 全部 部分组织的测试 既有全部又有list
        //应用全部
        testResourcePoolRequest.setAllOrg(true);
        setResources(testResourcePoolRequest, false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_ADD)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        //应用全部 关系表里不会存值
        TestResourcePool testResourcePool = getResult(mvcResult);
        return testResourcePool.getId();
    }

    @Test
    @Order(0)
    public void pluginSubTypeTest() throws Exception {
        String id = this.addPool(ResourcePoolTypeEnum.K8S.name());
        TaskRequest request = new TaskRequest();
        request.setPoolId(id);

        final ApiEngine engine = EngineFactory.createApiEngine(request);
        engine.start();
    }
}
