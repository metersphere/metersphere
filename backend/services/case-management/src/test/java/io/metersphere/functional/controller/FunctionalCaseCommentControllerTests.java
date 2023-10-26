package io.metersphere.functional.controller;

import com.jayway.jsonpath.JsonPath;
import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.domain.FunctionalCaseCommentExample;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.metersphere.functional.mapper.FunctionalCaseCommentMapper;
import io.metersphere.functional.mapper.FunctionalCaseCustomFieldMapper;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.project.domain.Notification;
import io.metersphere.project.domain.NotificationExample;
import io.metersphere.project.mapper.NotificationMapper;
import io.metersphere.sdk.constants.CustomFieldType;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.constants.TemplateScopeType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.CustomField;
import io.metersphere.system.mapper.CustomFieldMapper;
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

    @Resource
    private FunctionalCaseCommentMapper functionalCaseCommentMapper;

    @Resource
    private CustomFieldMapper customFieldMapper;

    @Resource
    private FunctionalCaseCustomFieldMapper functionalCaseCustomFieldMapper;

    public static final String SAVE_URL = "/functional/case/comment/save";
    public static final String DELETE_URL = "/functional/case/comment/delete/";


    private static String sessionId;
    private static String csrfToken;
    private static final String projectId = "100001100001";

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
    public void saveCommentATSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-1");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "default-project-member-user-guo-1"));
        System.out.println(notifications.get(0).getContent());
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo-1"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(2)
    public void saveCommentATFalse() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestNo");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-1");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        ResultHolder resultHolder = postFalse(functionalCaseCommentRequest);
        String jsonString = JSON.toJSONString(resultHolder.getData());
        System.out.println(jsonString);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTestNo").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(notifications));
    }

    private ResultHolder postFalse(FunctionalCaseCommentRequest functionalCaseCommentRequest) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SAVE_URL).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .content(JSON.toJSONString(functionalCaseCommentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JSON.parseObject(contentAsString, ResultHolder.class);
    }

    @Test
    @Order(3)
    public void saveCommentATExcludeSelfSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo");
        functionalCaseCommentRequest.setContent("这个好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK).andReceiverEqualTo("default-project-member-user-guo");
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(notifications));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "这个好"));
    }

    @Test
    @Order(4)
    public void saveCommentATNoNotifierSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("这个好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTestOne").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExample(notificationExample);
        Assertions.assertTrue(CollectionUtils.isEmpty(notifications));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTestOne"));
        Assertions.assertTrue(StringUtils.isBlank(functionalCaseComment.getNotifier()));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "这个好"));
    }


    @Test
    @Order(5)
    public void saveOnlyCommentSuccess() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.COMMENT);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTestOne").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        System.out.println(JSON.toJSONString(notifications));
        Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "gyq"));
        System.out.println(notifications.get(0).getContent());
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTestOne"));
        Assertions.assertTrue(StringUtils.isBlank(functionalCaseComment.getNotifier()));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(6)
    public void saveCommentReplySuccess() throws Exception {
        FunctionalCaseComment functionalCaseComment1 = getFunctionalCaseComment();
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setReplyUser("default-project-member-user-guo");
        functionalCaseCommentRequest.setParentId(functionalCaseComment1.getId());
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTestOne").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        System.out.println(JSON.toJSONString(notifications));
        Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "gyq"));
        System.out.println(notifications.get(0).getContent());
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTestOne"));
        Assertions.assertTrue(StringUtils.isBlank(functionalCaseComment.getNotifier()));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(7)
    public void saveCommentReplyNoParentId() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        ResultHolder resultHolder = postFalse(functionalCaseCommentRequest);
        String message = resultHolder.getMessage();
        Assertions.assertTrue(StringUtils.equals(message, Translator.get("case_comment.parent_id_is_null")));

    }

    @Test
    @Order(8)
    public void saveCommentReplyNoParent() throws Exception {
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setParentId("noComment");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        ResultHolder resultHolder = postFalse(functionalCaseCommentRequest);
        String message = resultHolder.getMessage();
        Assertions.assertTrue(StringUtils.equals(message, Translator.get("case_comment.parent_case_is_null")));


    }

    @Test
    @Order(9)
    public void saveCommentReplyNotifierSuccess() throws Exception {
        FunctionalCaseComment functionalCaseComment1 = getFunctionalCaseComment();
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-2");
        functionalCaseCommentRequest.setReplyUser("default-project-member-user-guo");
        functionalCaseCommentRequest.setParentId(functionalCaseComment1.getId());
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        System.out.println(JSON.toJSONString(notifications));
        Assertions.assertTrue(StringUtils.equals(notifications.get(1).getReceiver(), "default-project-member-user-guo-2"));
        System.out.println(notifications.get(0).getContent());
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.isNotBlank(functionalCaseComment.getNotifier()));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(10)
    public void saveCommentReplyNoReply() throws Exception {
        FunctionalCaseComment functionalCaseComment1 = getFunctionalCaseComment();
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTestOne");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setParentId(functionalCaseComment1.getId());
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        ResultHolder resultHolder = postFalse(functionalCaseCommentRequest);
        String message = resultHolder.getMessage();
        Assertions.assertTrue(StringUtils.equals(message, Translator.get("case_comment.reply_user_is_null")));
    }


    @Test
    @Order(11)
    public void saveCommentWidthCustomFields() throws Exception {
        CustomField customField = new CustomField();
        customField.setId("gyq_custom_field_one");
        customField.setName("testLevel");
        customField.setType(CustomFieldType.INPUT.toString());
        customField.setScene(TemplateScene.FUNCTIONAL.name());
        customField.setCreateUser("gyq");
        customField.setCreateTime(System.currentTimeMillis());
        customField.setUpdateTime(System.currentTimeMillis());
        customField.setRefId("gyq_custom_field_one");
        customField.setScopeId(projectId);
        customField.setScopeType(TemplateScopeType.PROJECT.name());
        customField.setInternal(false);
        customField.setEnableOptionKey(false);
        customField.setRemark("1");
        customFieldMapper.insertSelective(customField);
        FunctionalCaseCustomField functionalCaseCustomField = new FunctionalCaseCustomField();
        functionalCaseCustomField.setCaseId("xiaomeinvGTest");
        functionalCaseCustomField.setFieldId("gyq_custom_field_one");
        functionalCaseCustomField.setValue("1");
        functionalCaseCustomFieldMapper.insertSelective(functionalCaseCustomField);

        FunctionalCaseComment functionalCaseComment1 = getFunctionalCaseComment();
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo-3;default-project-member-user-guo-4;");
        functionalCaseCommentRequest.setContent("评论你好");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.REPLY);
        functionalCaseCommentRequest.setReplyUser("default-project-member-user-guo");
        functionalCaseCommentRequest.setParentId(functionalCaseComment1.getId());
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
       // Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "default-project-member-user-guo-1"));
        System.out.println(JSON.toJSONString(notifications));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo-3;default-project-member-user-guo-4;"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好"));
    }

    @Test
    @Order(12)
    public void saveCommentATWidthReplyUser() throws Exception {
        FunctionalCaseComment functionalCaseComment1 = getFunctionalCaseComment();
        FunctionalCaseCommentRequest functionalCaseCommentRequest = new FunctionalCaseCommentRequest();
        functionalCaseCommentRequest.setCaseId("xiaomeinvGTest");
        functionalCaseCommentRequest.setNotifier("default-project-member-user-guo;default-project-member-user-guo-4;");
        functionalCaseCommentRequest.setContent("评论你好哇");
        functionalCaseCommentRequest.setEvent(NoticeConstants.Event.AT);
        functionalCaseCommentRequest.setReplyUser("default-project-member-user-guo");
        functionalCaseCommentRequest.setParentId(functionalCaseComment1.getId());
        FunctionalCaseComment functionalCaseComment = getFunctionalCaseComment(functionalCaseCommentRequest);
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andResourceIdEqualTo("xiaomeinvGTest").andResourceTypeEqualTo(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK);
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(notificationExample);
        Assertions.assertFalse(notifications.isEmpty());
        // Assertions.assertTrue(StringUtils.equals(notifications.get(0).getReceiver(), "default-project-member-user-guo-1"));
        System.out.println(JSON.toJSONString(notifications));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getCaseId(), "xiaomeinvGTest"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getNotifier(), "default-project-member-user-guo;default-project-member-user-guo-4;"));
        Assertions.assertTrue(StringUtils.equals(functionalCaseComment.getContent(), "评论你好哇"));
    }

    @Test
    @Order(13)
    public void deleteCommentSuccess() throws Exception {
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdEqualTo("xiaomeinvGTest").andNotifierEqualTo("default-project-member-user-guo;default-project-member-user-guo-4;");
        List<FunctionalCaseComment> functionalCaseComments = functionalCaseCommentMapper.selectByExample(functionalCaseCommentExample);
        System.out.println(JSON.toJSONString(functionalCaseComments));
        String id = functionalCaseComments.get(0).getId();
        Assertions.assertFalse(functionalCaseComments.isEmpty());
        delFunctionalCaseComment(id);
        functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andParentIdEqualTo(id);
        List<FunctionalCaseComment> functionalCaseComments1 = functionalCaseCommentMapper.selectByExample(functionalCaseCommentExample);
        Assertions.assertTrue(functionalCaseComments1.isEmpty());
        FunctionalCaseComment functionalCaseComment = functionalCaseCommentMapper.selectByPrimaryKey(id);
        Assertions.assertNull(functionalCaseComment);

    }

    @Test
    @Order(14)
    public void deleteNoCommentSuccess() throws Exception {
        delFunctionalCaseComment("no_comment");
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andParentIdEqualTo("no_comment");
        List<FunctionalCaseComment> functionalCaseComments1 = functionalCaseCommentMapper.selectByExample(functionalCaseCommentExample);
        Assertions.assertTrue(functionalCaseComments1.isEmpty());
        FunctionalCaseComment functionalCaseComment = functionalCaseCommentMapper.selectByPrimaryKey("no_comment");
        Assertions.assertNull(functionalCaseComment);

    }

    private FunctionalCaseComment getFunctionalCaseComment(FunctionalCaseCommentRequest functionalCaseCommentRequest) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(SAVE_URL).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .content(JSON.toJSONString(functionalCaseCommentRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), FunctionalCaseComment.class);
    }

    private void delFunctionalCaseComment(String commentId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(DELETE_URL+commentId).header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .header(SessionConstants.CURRENT_PROJECT, projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    private FunctionalCaseComment getFunctionalCaseComment() {
        FunctionalCaseCommentExample functionalCaseCommentExample = new FunctionalCaseCommentExample();
        functionalCaseCommentExample.createCriteria().andCaseIdEqualTo("xiaomeinvGTest");
        List<FunctionalCaseComment> functionalCaseComments = functionalCaseCommentMapper.selectByExample(functionalCaseCommentExample);
        return functionalCaseComments.get(0);
    }

}
