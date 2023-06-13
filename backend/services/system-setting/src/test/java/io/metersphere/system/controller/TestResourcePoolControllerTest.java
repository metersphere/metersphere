package io.metersphere.system.controller;

import base.BaseTest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.TestResource;
import io.metersphere.system.dto.ResourcePoolTypeEnum;
import io.metersphere.system.dto.TestResourcePoolDTO;
import io.metersphere.system.request.QueryResourcePoolRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 class TestResourcePoolControllerTest extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void addTestResourcePool() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool_1");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        setResources(testResourcePoolDTO);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        //获取数据
        JSONObject jsonObject = new JSONObject(Boolean.parseBoolean(mvcResult.getResponse().getContentAsString()));
        JSONArray jsonArrayData = (JSONArray) jsonObject.get("data");
        System.out.println(jsonArrayData);
    }

    @Test
    @Order(2)
    void addUiTestResourcePoolFiled() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool_filed");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        testResourcePoolDTO.setUiTest(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(3)
    void listResourcePools() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setName("test_pool");
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/page")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(4)
    void updateTestResourcePool() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        setResources(testResourcePoolDTO);
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/update")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private static void setResources(TestResourcePoolDTO testResourcePoolDTO) {
        TestResource testResource = new TestResource();
        testResource.setId(UUID.randomUUID().toString());
        testResource.setTestResourcePoolId(UUID.randomUUID().toString());
        testResource.setEnable(true);
        testResource.setDeleted(false);
        List<TestResource> testResources = new ArrayList<>();
        testResources.add(testResource);
        testResourcePoolDTO.setTestResources(testResources);
    }

    @Test
    @Order(5)
    void updateTestResourcePoolFiled() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/update")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql(scripts = {"/sql/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Order(6)
    void deleteTestResourcePool() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/delete/102")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }


}