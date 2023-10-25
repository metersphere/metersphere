package io.metersphere.functional.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.notice.constants.NoticeConstants;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseCommentControllerTests {
    @Resource
    private MockMvc mockMvc;

    @Resource
    private NotificationMapper notificationMapper;

    public static final String SAVE_URL = "/functional/case/comment/save";

    private static String sessionId;
    private static String csrfToken;
    private static String projectId = "100001100001";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_case_comment.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void login() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content("{\"username\":\"default-project-member-user-guo\",\"password\":\"metersphere\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        sessionId = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.sessionId");
        csrfToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.data.csrfToken");
    }

    @Test
    @Order(1)
    public void saveCommentSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-1");
        functionalCaseCommentRequest.setContent("评论你好");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SAVE_URL).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .content(JSON.toJSONString(functionalCaseCommentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        FunctionalCaseComment functionalCaseComment = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCaseComment.class);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertTrue(notifications.size() > 0);
        Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "default-project-member-user-guo-1"));
        System.out.println(notifications.get(0).getContent());
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo-1"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(2)
    public void saveCommentFalse() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestNo");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-1");
        functionalCaseCommentRequest.setContent("评论你好");
        mockMvc.perform(MockMvcRequestBuilders.post(SAVE_URL).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .content(JSON.toJSONString(functionalCaseCommentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTestNo").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(notifications));
    }

    @Test
    @Order(3)
    public void saveCommentExcludeSelfSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo");
        functionalCaseCommentRequest.setContent("这个好");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SAVE_URL).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .content(JSON.toJSONString(functionalCaseCommentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        FunctionalCaseComment functionalCaseComment = JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCaseComment.class);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK).andReceiverEqualTo("default-project-member-user-guo");
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(notifications));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "这个好"));
    }

}
