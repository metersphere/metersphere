package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectApplication;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ProjectApplicationControllerTests {
    @Resource
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void testAddApp() throws Exception {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setProjectId("1");
        projectApplication.setType("1");
        projectApplication.setTypeValue("1");
        mockMvc.perform(MockMvcRequestBuilders.post("/project/application/save")
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
                        .content(JSON.toJSONString(projectApplication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.typeValue").value("2"));
    }

    @Test
    @Order(3)
    public void testListApp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/application/list/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].typeValue").value("2"))
                .andDo(print());
    }

}
