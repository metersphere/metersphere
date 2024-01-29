package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.service.CleanupRobotResourceService;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupRobotResourceTests extends BaseTest {
    @Resource
    private CleanupRobotResourceService resourceService;

    public static final String ROBOT_LIST = "/project/robot/list/";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_robot.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testCleanupResource() throws Exception {
        List<ProjectRobot> projectRobots = getList();
        if (CollectionUtils.isNotEmpty(projectRobots)) {
            resourceService.deleteResources("test");
        }
        List<ProjectRobot> projectRobotAfters = getList();
        Assertions.assertTrue(CollectionUtils.isEmpty(projectRobotAfters));
    }
    

    private List<ProjectRobot> getList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_LIST + "test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(sortHolder.getData()), ProjectRobot.class);
    }
}
