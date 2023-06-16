package io.metersphere.system.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.AuthSource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthSourceControllerTest {

    @Resource
    private MockMvc mockMvc;

    private static String sessionId;
    private static String csrfToken;

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
    public void testAddSource() throws Exception {
        AuthSource authSource = new AuthSource();
        authSource.setId("2b6a83d0-7c66-43ed-a1d9-5132d3167aaf");
        authSource.setConfiguration("123".getBytes());
        authSource.setName("测试CAS");
        authSource.setCreateTime(System.currentTimeMillis());
        authSource.setUpdateTime(System.currentTimeMillis());
        mockMvc.perform(MockMvcRequestBuilders.post("/system/authsource/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(authSource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    @Order(2)
    public void testGetSourceList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/system/authsource/list/1/10")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(3)
    public void testUpdateSource() throws Exception {
        AuthSource authSource = new AuthSource();
        authSource.setId("2b6a83d0-7c66-43ed-a1d9-5132d3167aaf");
        authSource.setConfiguration("123666".getBytes());
        authSource.setName("更新");
        authSource.setUpdateTime(System.currentTimeMillis());
        mockMvc.perform(MockMvcRequestBuilders.post("/system/authsource/update")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(authSource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }



    @Test
    @Order(4)
    public void testGetSourceById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/system/authsource/get/2b6a83d0-7c66-43ed-a1d9-5132d3167aaf")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(5)
    public void testDelSourceById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/system/authsource/delete/2b6a83d0-7c66-43ed-a1d9-5132d3167aaf")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Order(6)
    public void testAddSourceByNullName() throws Exception {
        AuthSource authSource = new AuthSource();
        authSource.setId("2b6a83d0-7c66-43ed-a1d9-5132d3167aaf");
        authSource.setConfiguration("123".getBytes());
        authSource.setCreateTime(System.currentTimeMillis());
        authSource.setUpdateTime(System.currentTimeMillis());
        mockMvc.perform(MockMvcRequestBuilders.post("/system/authsource/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(authSource))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andDo(print());
    }
}
