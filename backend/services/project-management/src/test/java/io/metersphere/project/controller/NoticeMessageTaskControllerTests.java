package io.metersphere.project.controller;


import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskBlob;
import io.metersphere.project.domain.MessageTaskExample;
import io.metersphere.project.dto.*;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.MessageTaskRequest;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoticeMessageTaskControllerTests extends BaseTest {

    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Resource
    private MessageTaskBlobMapper messageTaskBlobMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_message.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void addMessageTaskUserNoExistSuccess() throws Exception {
        //projectId 存在 用户有部分被删除的测试
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-1");
        userIds.add("project-message-user-2");
        userIds.add("project-message-user-3");
        userIds.add("project-message-user-del");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        messageTaskRequest.setSubject("看看改不改");
        messageTaskRequest.setUseDefaultSubject(false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(102001, resultHolder.getCode());
    }

    @Test
    @Order(2)
    public void addMessageTaskCheckUserSuccess() throws Exception {
        //projectId存在 user都存在没有被删除
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-3");
        userIds.add("project-message-user-4");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        messageTaskRequest.setTemplate("发送消息测试");
        messageTaskRequest.setUseDefaultTemplate(true);
        messageTaskRequest.setSubject("发送消息测试标题");
        messageTaskRequest.setUseDefaultTemplate(true);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());



        messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        userIds = new ArrayList<>();
        userIds.add("project-message-user-1");
        messageTaskRequest.setReceiverIds(userIds);
        mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());

        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria()
                .andProjectIdEqualTo("project-message-test").andProjectRobotIdEqualTo("test_message_robot2").andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK).andEventEqualTo(NoticeConstants.Event.CREATE);
        List<MessageTask> messageTasks1 = messageTaskMapper.selectByExample(messageTaskExample);
        Assertions.assertTrue(messageTasks1.getFirst().getEnable());
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(messageTasks1.getFirst().getSubject(), "看看改不改"));

        messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo("project-message-test").andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK).andEventEqualTo(NoticeConstants.Event.CREATE);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        Assertions.assertEquals(1, messageTasks.size());

        //projectId 存在 用户有部分被删除的测试
       messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        userIds = new ArrayList<>();
        userIds.add("project-message-user-1");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot5");
        messageTaskRequest.setEnable(true);
        messageTaskRequest.setSubject("看看改不改");
        messageTaskRequest.setUseDefaultSubject(false);
        this.requestPostWithOk("/notice/message/task/save",messageTaskRequest);
        messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo("project-message-test").andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK).andEventEqualTo(NoticeConstants.Event.CREATE).andProjectRobotIdEqualTo("test_message_robot5");
        messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        Assertions.assertEquals(1, messageTasks.size());
    }

    @Test
    @Order(3)
    public void addMessageTaskCheckUserFile() throws Exception {
        //用户都不存在
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-X");
        userIds.add("project-message-user-Y");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(102001, resultHolder.getCode());
    }

    @Test
    @Order(4)
    public void addMessageTaskCheckRobotSuccess() throws Exception {
        //使用默认机器人 使用默认模版
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-7");
        userIds.add("project-message-user-8");
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setEnable(null);
        messageTaskRequest.setTestId("testId");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK)
                .andEventEqualTo(NoticeConstants.Event.CREATE).andProjectIdEqualTo("project-message-test-1");
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        for (MessageTask messageTask : messageTasks) {
            if (messageTask.getReceivers().contains("project-message-user-8")) {
                Assertions.assertEquals("test_message_robot2", messageTask.getProjectRobotId());
                Assertions.assertEquals(false, messageTask.getEnable());
            }
        }
    }

    @Test
    @Order(5)
    public void addMessageTaskCheckOldExistSuccess() throws Exception {
        //更新使用默认模版
        //使用默认机器人 使用默认模版
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-7");
        userIds.add("project-message-user-8");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setEnable(null);
        messageTaskRequest.setTestId("testId");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK)
                .andEventEqualTo(NoticeConstants.Event.CREATE).andProjectIdEqualTo("project-message-test-1");
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        for (MessageTask messageTask : messageTasks) {
            if (messageTask.getReceivers().contains("project-message-user-8")) {
                Assertions.assertEquals("test_message_robot2", messageTask.getProjectRobotId());
                Assertions.assertEquals(false, messageTask.getEnable());
            }
        }

        Assertions.assertEquals("test_message_robot2", messageTasks.getFirst().getProjectRobotId());
        Assertions.assertEquals(false, messageTasks.getFirst().getEnable());
    }

    @Test
    @Order(6)
    public void addMessageTaskWithTemplateAndSubject() throws Exception {
        //项目不存在
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-9");
        userIds.add("project-message-user-10");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot1");
        messageTaskRequest.setEnable(true);
        messageTaskRequest.setTemplate("测试新加数据模版生效");
        messageTaskRequest.setUseDefaultTemplate(false);
        messageTaskRequest.setSubject("测试新加数据模版标题生效");
        messageTaskRequest.setUseDefaultSubject(false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andTaskTypeEqualTo(NoticeConstants.TaskType.API_DEFINITION_TASK)
                .andEventEqualTo(NoticeConstants.Event.CREATE).andProjectIdEqualTo("project-message-test-1").andProjectRobotIdEqualTo("test_message_robot1");
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        for (MessageTask messageTask : messageTasks) {
            if (messageTask.getReceivers().contains("project-message-user-9")) {
                MessageTaskBlob messageTaskBlob = messageTaskBlobMapper.selectByPrimaryKey(messageTask.getId());
                Assertions.assertEquals("测试新加数据模版生效", messageTaskBlob.getTemplate());
                Assertions.assertEquals("测试新加数据模版标题生效", messageTask.getSubject());
            }
        }

    }

    @Test
    @Order(7)
    public void addMessageTaskCheckProjectExistFail() throws Exception {
        //项目不存在
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-x");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-7");
        userIds.add("project-message-user-8");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot1");
        messageTaskRequest.setEnable(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(8)
    public void getMessageListSuccess() throws Exception {
        setMessageTask("project-message-test-3", "test_message_robot4");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/project-message-test-3")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<MessageTaskDTO> messageTaskDetailDTOList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
        System.out.println(messageTaskDetailDTOList);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(messageTaskDetailDTOList));
        System.out.println(messageTaskDetailDTOList);
    }

    @Test
    @Order(10)
    public void getMessageListEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/project-message-test-2")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<MessageTaskDTO> messageTaskDetailDTOList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
        MessageTaskDetailDTO messageTaskDetailDTO = messageTaskDetailDTOList.getFirst().getMessageTaskTypeDTOList().getFirst().getMessageTaskDetailDTOList().getFirst();
        Map<String, ProjectRobotConfigDTO> projectRobotConfigMap = messageTaskDetailDTO.getProjectRobotConfigMap();
        System.out.println(projectRobotConfigMap);
        // Assertions.assertTrue(StringUtils.isBlank(robotId));

    }

    @Test
    @Order(11)
    public void addMessageTaskCheckSpecialUserSuccess() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        userIds.add(NoticeConstants.RelatedUser.OPERATOR);
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        //messageTaskRequest.setDefaultTemplate("发送消息测试");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
    }

    @Test
    @Order(12)
    public void addMessageTaskCheckSpecialUserTwoSuccess() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        userIds.add("project-message-user-5");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        // messageTaskRequest.setDefaultTemplate("发送消息测试");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
    }

    @Test
    @Order(13)
    public void addMessageTaskCheckSpecialUserThreeSuccess() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        userIds.add("project-message-user-6");
        userIds.add("project-message-user-del");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(false);
        //messageTaskRequest.setDefaultTemplate("发送消息测试");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(102001, resultHolder.getCode());
    }

    @Test
    @Order(14)
    public void updateMessageTaskCheckSpecialUserThreeSuccess() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        userIds.add("project-message-user-6");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(false);
        messageTaskRequest.setTemplate("发送消息测试");
        messageTaskRequest.setSubject("发送消息测试标题");
        messageTaskRequest.setUseDefaultTemplate(false);
        messageTaskRequest.setUseDefaultSubject(false);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
    }

    @Test
    @Order(15)
    public void getMessageListUpdate() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<MessageTaskDTO> messageTaskDetailDTOList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
        System.out.println(messageTaskDetailDTOList);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(messageTaskDetailDTOList));
        System.out.println(messageTaskDetailDTOList);
        List<MessageTaskDTO> collect = messageTaskDetailDTOList.stream().filter(t -> t.type.equals(NoticeConstants.Module.API_TEST_MANAGEMENT)).toList();
        List<MessageTaskTypeDTO> messageTaskTypeDTOList = collect.getFirst().getMessageTaskTypeDTOList();
        List<MessageTaskTypeDTO> collect1 = messageTaskTypeDTOList.stream().filter(t -> t.getTaskType().equals(NoticeConstants.TaskType.API_DEFINITION_TASK)).toList();
        List<MessageTaskDetailDTO> messageTaskDetailDTOList1 = collect1.getFirst().getMessageTaskDetailDTOList();
        List<MessageTaskDetailDTO> collect2 = messageTaskDetailDTOList1.stream().filter(t -> t.event.equals(NoticeConstants.Event.CREATE)).toList();
        Map<String, ProjectRobotConfigDTO> projectRobotConfigMap = collect2.getFirst().getProjectRobotConfigMap();
        ProjectRobotConfigDTO projectRobotConfigDTO = projectRobotConfigMap.get("test_message_robot2");
        Assertions.assertTrue(StringUtils.equals(projectRobotConfigDTO.getTemplate(),"发送消息测试"));
    }


    @Test
    @Order(16)
    public void closeMessageTaskSuccess() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(false);
        //messageTaskRequest.setDefaultTemplate("发送消息测试");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        Assertions.assertEquals(100200, resultHolder.getCode());
    }

    @Test
    @Order(17)
    public void getUserSuccess() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/user/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("keyword", "project-message-user-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> userDtoList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(userDtoList.size() > 0);
    }

    @Test
    @Order(18)
    public void getUserSuccessAll() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/user/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> userDtoList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertTrue(userDtoList.size() > 0);

    }

    @Test
    @Order(19)
    public void getUserSuccessEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/user/project-message-test-x")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<OptionDTO> userDtoList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), OptionDTO.class);
        Assertions.assertEquals(4, userDtoList.size());
    }

    @Test
    @Order(20)
    public void getTemplateDetailWithRobot() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/template/detail/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("taskType", NoticeConstants.TaskType.API_DEFINITION_TASK)
                        .param("event", NoticeConstants.Event.CREATE)
                        .param("robotId", "test_message_robot2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        MessageTemplateConfigDTO messageTemplateConfigDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), MessageTemplateConfigDTO.class);
        Assertions.assertTrue(messageTemplateConfigDTO.getReceiverIds().size() > 0);
    }

    @Test
    @Order(21)
    public void getTemplateDetailWithOutRobot() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/template/detail/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("taskType", NoticeConstants.TaskType.API_DEFINITION_TASK)
                        .param("event", NoticeConstants.Event.CREATE)
                        .param("robotId", "test_message_robot3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        MessageTemplateConfigDTO messageTemplateConfigDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), MessageTemplateConfigDTO.class);
        Assertions.assertTrue(messageTemplateConfigDTO.getReceiverIds().size() > 0);
    }

    @Test
    @Order(22)
    public void getTemplateDetailNotExistRobot() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/template/detail/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("taskType", NoticeConstants.TaskType.API_DEFINITION_TASK)
                        .param("event", NoticeConstants.Event.CREATE)
                        .param("robotId", "test_message_robotX")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Test
    @Order(23)
    public void getTemplateDetailNotData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/template/detail/project-message-test")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .param("taskType", NoticeConstants.TaskType.BUG_TASK)
                        .param("event", NoticeConstants.Event.CREATE)
                        .param("robotId", "test_message_robot3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        MessageTemplateConfigDTO messageTemplateConfigDTO = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), MessageTemplateConfigDTO.class);
        Assertions.assertTrue(StringUtils.isNotBlank(messageTemplateConfigDTO.getTemplate()));
        Assertions.assertTrue(CollectionUtils.isEmpty(messageTemplateConfigDTO.getReceiverIds()));

    }

    public void setMessageTask(String projectId, String defaultRobotId) {
        StringBuilder jsonStr = new StringBuilder();

        try {
            InputStream inputStream = getClass().getResourceAsStream("/message_task.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);

        List<MessageTaskDTO> messageTaskDTOList = JSON.parseArray(jsonStr.toString(), MessageTaskDTO.class);
        for (MessageTaskDTO messageTaskDTO : messageTaskDTOList) {
            List<MessageTaskTypeDTO> messageTaskTypeDTOList = messageTaskDTO.getMessageTaskTypeDTOList();
            for (MessageTaskTypeDTO messageTaskTypeDTO : messageTaskTypeDTOList) {
                String taskType = messageTaskTypeDTO.getTaskType();
                if (taskType.contains(NoticeConstants.Mode.SCHEDULE) || taskType.contains("AT")) {
                    continue;
                }
                List<MessageTaskDetailDTO> messageTaskDetailDTOList = messageTaskTypeDTO.getMessageTaskDetailDTOList();
                for (MessageTaskDetailDTO messageTaskDetailDTO : messageTaskDetailDTOList) {
                    String event = messageTaskDetailDTO.getEvent();
                    List<OptionDTO> receivers = messageTaskDetailDTO.getReceivers();
                    if (StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CREATE) || StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CASE_CREATE) || StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.MOCK_CREATE) || CollectionUtils.isEmpty(receivers)) {
                        continue;
                    }
                    List<String> receiverIds = receivers.stream().map(OptionDTO::getId).toList();
                    String id = IDGenerator.nextStr();
                    MessageTask messageTask = new MessageTask();
                    messageTask.setId(id);
                    messageTask.setEvent(event);
                    messageTask.setTaskType(taskType);
                    messageTask.setReceivers(receiverIds);
                    messageTask.setProjectId(projectId);
                    messageTask.setProjectRobotId(defaultRobotId);
                    messageTask.setEnable(true);
                    messageTask.setTestId("NONE");
                    messageTask.setCreateUser("admin");
                    messageTask.setCreateTime(System.currentTimeMillis());
                    messageTask.setUpdateUser("admin");
                    messageTask.setUpdateTime(System.currentTimeMillis());
                    messageTask.setSubject("");
                    messageTask.setUseDefaultSubject(true);
                    messageTask.setUseDefaultTemplate(true);
                    MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
                    messageTaskBlob.setId(id);
                    messageTaskBlob.setTemplate("");
                    mapper.insert(messageTask);
                    blobMapper.insert(messageTaskBlob);
                }
            }
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }
}
