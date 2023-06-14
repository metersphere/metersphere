package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.TestResource;
import io.metersphere.system.dto.ResourcePoolTypeEnum;
import io.metersphere.system.dto.TestResourcePoolDTO;
import io.metersphere.system.request.QueryResourcePoolRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestResourcePoolControllerTest extends BaseTest {

    @Resource
    private MockMvc mockMvc;

    private static final String TEST_RESOURCE_POOL_ADD = "/test/resource/pool/add";
    private static final String TEST_RESOURCE_POOL_UPDATE = "/test/resource/pool/update";

    private static final ResultMatcher ERROR_REQUEST_MATCHER = status().is5xxServerError();

    @Test
    @Order(1)
    void addTestResourcePool() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool_1");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        setResources(testResourcePoolDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_ADD)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(2)
    void addUiTestResourcePoolFiled() throws Exception {
        //资源池名称为空
        TestResourcePoolDTO testResourcePoolDTO = generatorDto(true, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池类型为空
        testResourcePoolDTO = generatorDto(false, true, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池节点集合为空
        testResourcePoolDTO = generatorDto(false, false, true, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //UI的grid为空
        testResourcePoolDTO = generatorDto(true, true, true, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);

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
        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_UPDATE)
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
        TestResourcePoolDTO testResourcePoolDTO = generatorDto(true, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池类型为空
        testResourcePoolDTO = generatorDto(false, true, false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池节点集合为空
        testResourcePoolDTO = generatorDto(false, false, true, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //UI的grid为空
        testResourcePoolDTO = generatorDto(true, true, true, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
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

    private void requestPost(String url, Object param, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(param))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private TestResourcePoolDTO generatorDto(boolean noName, boolean noType, boolean noResources, boolean noUiGrid) {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        //没名字
        if (!noName) {
            testResourcePoolDTO.setName("test_pool_test");
        }
        //没类型
        if (!noType) {
            testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        }
        //没资源池
        if (!noResources) {
            setResources(testResourcePoolDTO);
        }

        testResourcePoolDTO.setUiTest(true);
        //没UI的grid
        if (!noUiGrid) {
            testResourcePoolDTO.setGrid("localhost:4444");
        }
        return testResourcePoolDTO;
    }


}