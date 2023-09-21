package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.request.ProjectRobotRequest;
import io.metersphere.project.service.CleanupRobotResourceService;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
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


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CleanupRobotResourceTests extends BaseTest {
    @Resource
    private CleanupRobotResourceService resourceService;

    public static final String ROBOT_LIST = "/project/robot/list/page";

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_robot.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testCleanupResource() throws Exception {
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        Pager<?> sortPageData = getPager(request);
        List<ProjectRobot> projectRobots = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectRobot.class);
        if (CollectionUtils.isNotEmpty(projectRobots)) {
            resourceService.deleteResources("test");
        }
        request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setKeyword("测试机器人");
        Pager<?> sortPageDataAfter = getPager(request);
        List<ProjectRobot> projectRobotAfters = JSON.parseArray(JSON.toJSONString(sortPageDataAfter.getList()), ProjectRobot.class);
        Assertions.assertTrue(CollectionUtils.isEmpty(projectRobotAfters));
    }

    @Test
    @Order(2)
    public void testCleanupReportResource() throws Exception {
        resourceService.cleanReportResources("test");
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
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        return sortPageData;
    }
}
