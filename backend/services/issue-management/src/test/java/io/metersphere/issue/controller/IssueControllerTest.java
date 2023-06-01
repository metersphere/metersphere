package io.metersphere.issue.controller;

import io.metersphere.issue.domain.Issue;
import io.metersphere.sdk.util.JSON;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class IssueControllerTest {

    @Resource
    private MockMvc mockMvc;

    @Test
    void listAll() {
    }

    @Test
    void get() {
    }

    @Test
    void add() throws Exception {
        Issue issue = new Issue();
        issue.setTitle("test");
        issue.setId(UUID.randomUUID().toString());
        issue.setProjectId(UUID.randomUUID().toString());
        issue.setCreateUser(UUID.randomUUID().toString());
        issue.setNum(1);
        issue.setPlatformStatus(UUID.randomUUID().toString());
        issue.setPlatform(UUID.randomUUID().toString());
        issue.setSourceId(UUID.randomUUID().toString());

        mockMvc.perform(
                MockMvcRequestBuilders.post("/issue/add")
                        .content(JSON.toJSONString(issue))
                        .contentType(MediaType.APPLICATION_JSON))
                // 检查状态
                .andExpect(status().isOk())
                // 检查响应头
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 检查数据
                .andExpect(jsonPath("$.data.title").value("test"))
                .andDo(print());

        // 缺陷已存在校验
        mockMvc.perform(
                MockMvcRequestBuilders.post("/issue/add")
                        .content(JSON.toJSONString(issue))
                        .contentType(MediaType.APPLICATION_JSON))
                // 检查失败状态码
                .andExpect(status().is5xxServerError())
                // 检查响应头
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 检查业务状态码
                .andExpect(jsonPath("$.code").value(108001))
                .andDo(print());
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}
