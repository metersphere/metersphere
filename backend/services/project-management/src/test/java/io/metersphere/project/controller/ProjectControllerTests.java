package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.project.domain.Project;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectControllerTests {
    @Resource
    private MockMvc mockMvc;

    private static String projectId;

    // 添加项目
    @Test
    @Order(1)
    public void testAddProject() throws Exception {
        Project project = new Project();
        project.setName("test");
        project.setCreateUser("admin");
        project.setWorkspaceId("default");

        var result = mockMvc.perform(MockMvcRequestBuilders.post("/project/add")
                        .content(JSON.toJSONString(project))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        projectId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    }

    @Test
    @Order(2)
    public void testEditProject() throws Exception {
        Project project = new Project();
        project.setId(projectId);
        project.setName("test2");
        project.setCreateUser("admin");
        project.setWorkspaceId("default");

        mockMvc.perform(MockMvcRequestBuilders.post("/project/update")
                        .content(JSON.toJSONString(project))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    @Order(3)
    public void testSelectAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/project/list_all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.person.name").value("Jason"))
                .andDo(print());
    }

}
