package io.metersphere.api.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests {
    @Resource
    private MockMvc mockMvc;
    private final static String prefix = "/api/definition";
    private static String sessionId;
    private static String csrfToken;

    @Test
    @BeforeEach
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
    public void testAdd() throws Exception {
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
        request.setProtocol("test-api-protocol");
        request.setPos(1l);
        request.setLatest(true);
        request.setStatus("test-api-status");
        request.setVersionId("test-api-version");
        request.setDeleted(false);

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/add")
                        .file(file)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("test-api-id"));
    }

    @Test
    @Order(2)
    public void testUpdate() throws Exception {
        LogUtils.info("delete api test");
        ApiDefinitionDTO request = new ApiDefinitionDTO();
        // 补充属性内容
        request.setId("test-api-id");
        request.setCreateUser("test-user");
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateUser("test-api-id");
        request.setUpdateTime(System.currentTimeMillis());
        request.setProjectId("test-project-id");
        request.setName("test-api-name");
        request.setProtocol("test-api-protocol");
        request.setPos(1l);
        request.setLatest(true);
        request.setStatus("test-api-status");
        request.setVersionId("test-api-version");
        request.setDeleted(false);
        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JSON.toJSONString(request))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());

    }

    @Test
    @Order(2)
    public void testBatchUpdate() throws Exception {
        LogUtils.info("delete api test");
        List<String> tests = new ArrayList<>();
        tests.add("test-api-id");

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/batch-update")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JSON.toJSONString(tests))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());

    }

    @Test
    @Order(4)
    public void testDel() throws Exception {
        LogUtils.info("delete api test");

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/delete")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("test-api-id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());

    }

    @Test
    @Order(5)
    public void testBatchDel() throws Exception {
        LogUtils.info("delete api test");
        List<String> tests = new ArrayList<>();
        tests.add("test-api-id");

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/batch-del")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(JSON.toJSONString(tests))
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk());

    }

}
