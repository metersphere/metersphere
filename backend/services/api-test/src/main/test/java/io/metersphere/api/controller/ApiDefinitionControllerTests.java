package io.metersphere.api.controller;

import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.utils.JsonUtils;
import jakarta.annotation.Resource;
import org.junit.Before;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests {
    @Resource
    private MockMvc mockMvc;
    private final static String prefix = "/api/definition";

    @Before
    public void init() {
        LogUtils.info("init base api test");
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
                        .content(JsonUtils.toJSONString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.id").value("test-api-id"));
    }
}
