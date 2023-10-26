package io.metersphere.project.controller;

import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskExample;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.invoker.ProjectServiceInvoker;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class CreateRobotResourceTests extends BaseTest {
    private final ProjectServiceInvoker serviceInvoker;

    public static final String ROBOT_LIST = "/project/robot/list/";

    @Autowired
    public CreateRobotResourceTests(ProjectServiceInvoker serviceInvoker) {
        this.serviceInvoker = serviceInvoker;
    }

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Test
    @Order(1)
    public void testCreateResource() throws Exception {
        String id = UUID.randomUUID().toString();
        Project project = new Project();
        project.setId(id);
        project.setOrganizationId("organization-message-test");
        project.setName("默认项目");
        project.setDescription("系统默认创建的项目");
        project.setCreateUser("admin");
        project.setUpdateUser("admin");
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.insertSelective(project);
        serviceInvoker.invokeCreateServices(id);
        List<ProjectRobot> projectRobotAfters = getList(id);
        Assertions.assertEquals(2, projectRobotAfters.size());
        List<MessageTaskDTO> messageList = getMessageList(id);
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(id).andEventLike("AT");
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        Assertions.assertTrue(messageTasks.size() > 0);
        Assertions.assertTrue(messageList.size() > 0);
    }

    private List<ProjectRobot> getList(String id) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_LIST + id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(sortHolder.getData()), ProjectRobot.class);
    }

    private List<MessageTaskDTO> getMessageList(String id) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/"+id)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
    }
}
