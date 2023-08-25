package io.metersphere.project.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.sdk.base.BaseTest;
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
public class ProjectControllerTests extends BaseTest {
    @Resource
    private MockMvc mockMvc;

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
    //                ;
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
    //                ;
    //    }

}
