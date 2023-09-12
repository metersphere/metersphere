package io.metersphere.project.controller;



import io.metersphere.sdk.base.BaseTest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.dto.request.MessageTaskRequest;
import io.metersphere.sdk.notice.constants.NoticeConstants;
import io.metersphere.sdk.util.JSON;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoticeMessageTaskControllerTests extends BaseTest {

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_project_message.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void addMessageTask() throws Exception {
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-message-test");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-message-user-1");
        userIds.add("project-message-user-2");
        userIds.add("project-message-user-del");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId("test_message_robot2");
        messageTaskRequest.setEnable(true);
        messageTaskRequest.setTemplate("发送消息测试");
        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/add")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        System.out.println(contentAsString1);
    }

}
