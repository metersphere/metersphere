package io.metersphere.bug.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.bug.domain.Bug;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugControllerTest {

    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;


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
    void listAll() {
    }

    @Test
    void get() {
    }

    @Test
    void add() throws Exception {
        Bug bug = new Bug();
        bug.setTitle("test");
        bug.setId(UUID.randomUUID().toString());
        bug.setProjectId(UUID.randomUUID().toString());
        bug.setCreateUser(UUID.randomUUID().toString());
        bug.setNum(1);
        bug.setPlatformStatus(UUID.randomUUID().toString());
        bug.setPlatform(UUID.randomUUID().toString());
        bug.setSourceId(UUID.randomUUID().toString());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/bug/add")
                                .header(SessionConstants.HEADER_TOKEN, sessionId)
                                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                                .content(JSON.toJSONString(bug))
                                .contentType(MediaType.APPLICATION_JSON))
                // 检查状态
                .andExpect(status().isOk())
                // 检查响应头
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 检查数据
                .andExpect(jsonPath("$.data.title").value("test"))
        ;

        // 缺陷已存在校验
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/bug/add")
                                .header(SessionConstants.HEADER_TOKEN, sessionId)
                                .header(SessionConstants.CSRF_TOKEN, csrfToken)
                                .content(JSON.toJSONString(bug))
                                .contentType(MediaType.APPLICATION_JSON))
                // 检查失败状态码
                .andExpect(status().is5xxServerError())
                // 检查响应头
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 检查业务状态码
                .andExpect(jsonPath("$.code").value(108001))
        ;
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}
