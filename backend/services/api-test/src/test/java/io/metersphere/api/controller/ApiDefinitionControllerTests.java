package io.metersphere.api.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.api.dto.ApiDefinitionListRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.Pager;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests {
    @Resource
    private MockMvc mockMvc;
    private final static String prefix = "/api/definition";
    private static String sessionId;
    private static String csrfToken;

    @Before
    public void init() {
        LogUtils.info("init base api test");
    }



    @Test
    @Order(0)
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content("{\"username\":\"admin\",\"password\":\"metersphere\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
    }

    @Test
    @Order(1)
    public void testCreate() throws Exception {
        LogUtils.info("create api test");
        // 创建一个 MockMultipartFile 对象，用于模拟文件上传
        MockMultipartFile file = new MockMultipartFile("files", "files", MediaType.APPLICATION_OCTET_STREAM_VALUE, "Test content".getBytes());

        // 创建一个 ApiDefinitionDTO 对象，用于模拟请求的一部分
        ApiDefinitionDTO request = new ApiDefinitionDTO();
        // 补充属性内容
        request.setId("test-api-id");
        request.setCreateUser("test-user");
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateUser("test-api-id");
        request.setUpdateTime(System.currentTimeMillis());
        request.setProjectId("test-project-id");
        request.setName("test-api-name");
        request.setMethod("test-api-method");
        request.setPath("test-api-path");
        request.setProtocol("test-api-protocol");
        request.setPos(1l);
        request.setLatest(true);
        request.setSyncEnable(true);
        request.setStatus("test-api-status");
        request.setVersionId("test-api-version");
        request.setDeleted(false);

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/add")
                        .file(file)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JsonUtils.toJSONString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("test-api-id"));
    }

    //正常接口获取
    @Test
    @Sql(scripts = {"/sql/init_api_definition.sql"},
            config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED),
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Order(2)
    public void listSuccess() throws Exception {
        int pageSize = 10;
        int current = 1;
        ApiDefinitionListRequest request = new ApiDefinitionListRequest();
        request.setCurrent(current);
        request.setPageSize(pageSize);
        request.setProjectId("test-project-id");

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/page")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.toJSONString(request))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse mockResponse = mvcResult.getResponse();

        String returnData = mockResponse.getContentAsString();
        ResultHolder resultHolder = JsonUtils.parseObject(returnData, ResultHolder.class);

        //返回请求正常
        Assertions.assertNotNull(resultHolder);

        Pager<ApiDefinition> returnPager = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), Pager.class);

        //返回值不为空
        Assertions.assertNotNull(returnPager);
        //返回值的页码和当前页码相同
        Assertions.assertEquals(returnPager.getCurrent(), current);
        //返回的数据量不超过规定要返回的数据量相同
        Assertions.assertTrue(((List<ApiDefinition>) returnPager.getList()).size() <= pageSize);
    }

    //没有传入必填值
    @Test
    @Order(3)
    public void listError() throws Exception {
        // projectId为空
        ApiDefinitionListRequest request = new ApiDefinitionListRequest();
        request.setCurrent(1);
        request.setPageSize(20);
        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/page")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.toJSONString(request))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isBadRequest());

        //pageSize为空
        request = new ApiDefinitionListRequest();
        request.setCurrent(1);
        request.setProjectId("test-project-id");
        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/page")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.toJSONString(request))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isBadRequest());

        //current为空
        request = new ApiDefinitionListRequest();
        request.setPageSize(20);
        request.setProjectId("test-project-id");
        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/page")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JsonUtils.toJSONString(request))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isBadRequest());
    }

}
