package io.metersphere.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.ApiDefinitionDTO;
import io.metersphere.sdk.util.LogUtils;
import jakarta.annotation.Resource;
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


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiDefinitionControllerTests {
    @Resource
    private MockMvc mockMvc;
    private final static String prefix = "/api/definition";

    @Test
    @Order(1)
    public void testCreate() throws Exception {
        LogUtils.info("create api test");
        // 创建一个 MockMultipartFile 对象，用于模拟文件上传
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "application/octet-stream", "Test content".getBytes());

        // 创建一个 ApiDefinitionDTO 对象，用于模拟请求的一部分
        ApiDefinitionDTO request = new ApiDefinitionDTO();
        // 补充属性内容
        request.setId("test-api-id");

        mockMvc.perform(MockMvcRequestBuilders.multipart(prefix + "/create")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("request", new ObjectMapper().writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().is(400));
        //.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        //.andExpect(MockMvcResultMatchers.jsonPath("$.id").value("test-api-id"));
    }
}
