package io.metersphere.system.controller;

import base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
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

    private static final String configuration = "{\n" +
            "  \"loadTestImage\": \"123\",\n" +
            "  \"loadTestHeap\": \"123\",\n" +
            "  \"nodesList\":[{\n" +
            "       \"ip\":\"172.2.130.1\",\n" +
            "       \"port\": \"3306\",\n" +
            "       \"monitor\": \"11\",\n" +
            "       \"concurrentNumber\": 1\n" +
            "   }],\n" +
            "\"ip\":\"172.2.130.1\",\n" +
            "\"token\":\"dsdfssdsvgsd\",\n" +
            "\"namespaces\":\"测试\",\n" +
            "\"concurrentNumber\":3,\n" +
            "\"podThreads\":2,\n" +
            "\"jobDefinition\":\"jsfsjs\",\n" +
            "\"apiTestImage\":\"ddgd\",\n" +
            "\"deployName\":\"hello\",\n" +
            "\"uiGrid\":\"localhost:4444\"\n" +
            "}";

    @Test
    @Order(1)
    void addTestResourcePool() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool_1");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        testResourcePoolDTO.setApiTest(true);
        testResourcePoolDTO.setLoadTest(false);
        testResourcePoolDTO.setUiTest(false);
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
        TestResourcePoolDTO testResourcePoolDTO = generatorDto(true, false, false, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池类型为空
        testResourcePoolDTO = generatorDto(false, true, false, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池节点集合为空
        testResourcePoolDTO = generatorDto(false, false, true, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //用途测试
        testResourcePoolDTO = generatorDto(false, false, false, true, false, false);
        this.requestPost(TEST_RESOURCE_POOL_ADD, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //ui用途为空
        testResourcePoolDTO = generatorDto(false, false, false, false,true, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //load用途为空
        testResourcePoolDTO = generatorDto(false, false, false, false,false, true);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Order(3)
    void listResourcePoolsWidthSearch() throws Exception {
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
    void listResourcePoolsNoSearch() throws Exception {
        QueryResourcePoolRequest request = new QueryResourcePoolRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        mockMvc.perform(MockMvcRequestBuilders.post("/test/resource/pool/page")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void updateTestResourcePool() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = new TestResourcePoolDTO();
        testResourcePoolDTO.setName("test_pool");
        testResourcePoolDTO.setType(ResourcePoolTypeEnum.NODE.name());
        setResources(testResourcePoolDTO);
        testResourcePoolDTO.setApiTest(true);
        testResourcePoolDTO.setLoadTest(false);
        testResourcePoolDTO.setUiTest(false);
        mockMvc.perform(MockMvcRequestBuilders.post(TEST_RESOURCE_POOL_UPDATE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(testResourcePoolDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private static void setResources(TestResourcePoolDTO testResourcePoolDTO) {
        testResourcePoolDTO.setConfiguration(configuration);
    }

    @Test
    @Order(6)
    void updateTestResourcePoolFiled() throws Exception {
        TestResourcePoolDTO testResourcePoolDTO = generatorDto(true, false, false,false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池类型为空
        testResourcePoolDTO = generatorDto(false, true, false, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //资源池节点集合为空
        testResourcePoolDTO = generatorDto(false, false, true, false, false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //api用途为空
        testResourcePoolDTO = generatorDto(false, false, false, true,false, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //ui用途为空
        testResourcePoolDTO = generatorDto(false, false, false, false,true, false);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
        //load用途为空
        testResourcePoolDTO = generatorDto(false, false, false, false,false, true);
        this.requestPost(TEST_RESOURCE_POOL_UPDATE, testResourcePoolDTO, ERROR_REQUEST_MATCHER);
    }

    @Test
    @Sql(scripts = {"/dml/init_test_resource_pool.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Order(7)
    void deleteTestResourcePool() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/delete/102")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(8)
    void deleteTestResourcePoolFiled() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/resource/pool/delete/105")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(ERROR_REQUEST_MATCHER)
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

    private TestResourcePoolDTO generatorDto(boolean noName, boolean noType, boolean noResources, boolean noUseApi, boolean noUseUi, boolean noUseLoad) {
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
        //没api
        if(!noUseApi) {
            testResourcePoolDTO.setApiTest(true);
        }
        if (!noUseUi) {
            testResourcePoolDTO.setUiTest(true);
        }
        if (!noUseLoad) {
            testResourcePoolDTO.setLoadTest(true);
        }
        return testResourcePoolDTO;
    }


}