package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.request.ProjectRobotRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CreateRobotResourceTests extends BaseTest {
    private final ProjectServiceInvoker serviceInvoker;

    public static final String ROBOT_LIST = "/project/robot/list/page";

    @Autowired
    public CreateRobotResourceTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Test
    @Order(1)
    public void testCleanupResource() throws Exception {
        serviceInvoker.invokeCreateServices("test");
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setProjectId("test");
        Pager<?> sortPageDataAfter = getPager(request);
        List<ProjectRobot> projectRobotAfters = JSON.parseArray(JSON.toJSONString(sortPageDataAfter.getList()), ProjectRobot.class);
        Assertions.assertEquals(2, projectRobotAfters.size());
    }

    private Pager<?> getPager(ProjectRobotRequest request) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ROBOT_LIST)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
    }
}
