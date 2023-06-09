package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectControllerTests {
    @Resource
    private MockMvc mockMvc;

    private static String projectId;
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

    // 添加项目
    //    @Test
    //    @Order(1)
    //    public void testAddProject() throws Exception {
    //        Project project = new Project();
    //        project.setName("test");
    //        project.setCreateUser("admin");
    //        project.setOrganizationId("default");
    //
    //
    //        var result = mockMvc.perform(MockMvcRequestBuilders.post("/project/add")
    //                        .header(SessionConstants.HEADER_TOKEN, sessionId)
    //                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
    //                        .content(JSON.toJSONString(project))
    //                        .contentType(MediaType.APPLICATION_JSON))
    //                .andExpect(status().isOk())
    //                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //                .andReturn();
    //        projectId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    //    }
    //
    //    @Test
    //    @Order(2)
    //    public void testEditProject() throws Exception {
    //        Project project = new Project();
    //        project.setId(projectId);
    //        project.setName("test2");
    //        project.setCreateUser("admin");
    //        project.setOrganizationId("default");
    //
    //        mockMvc.perform(MockMvcRequestBuilders.post("/project/edit")
    //                        .header(SessionConstants.HEADER_TOKEN, sessionId)
    //                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
    //                        .content(JSON.toJSONString(project))
    //                        .contentType(MediaType.APPLICATION_JSON))
    //                .andExpect(status().isOk())
    //                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //                .andDo(print());
    //    }
    //
    //    @Test
    //    @Order(3)
    //    public void testSelectAll() throws Exception {
    //        mockMvc.perform(MockMvcRequestBuilders.get("/project/list-all")
    //                        .header(SessionConstants.HEADER_TOKEN, sessionId)
    //                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
    //                .andExpect(status().isOk())
    //                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //                //                .andExpect(jsonPath("$.person.name").value("Jason"))
    //                .andDo(print());
    //    }

}
