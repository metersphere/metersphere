package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests {
    @Resource
    private MockMvc mockMvc;
    private static String sessionId;
    private static String csrfToken;


    @Test
    @Order(0)
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/signin")
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
    public void testAddApp() throws Exception {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId("1");
        projectApplication.setType("1");
        projectApplication.setTypeValue("1");
        mockMvc.perform(MockMvcRequestBuilders.post("/project/application/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(projectApplication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.typeValue").value("1"));
    }

    @Test
    @Order(2)
    public void testUpdateApp() throws Exception {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId("1");
        projectApplication.setType("1");
        projectApplication.setTypeValue("2");
        mockMvc.perform(MockMvcRequestBuilders.post("/project/application/update")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(projectApplication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.typeValue").value("2"));
    }

    @Test
    @Order(3)
    public void testListApp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/application/list/1")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].typeValue").value("2"));
    }

}
