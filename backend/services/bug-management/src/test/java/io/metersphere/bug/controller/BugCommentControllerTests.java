package io.metersphere.bug.controller;

import io.metersphere.bug.domain.BugComment;
import io.metersphere.bug.dto.request.BugCommentEditRequest;
import io.metersphere.bug.dto.response.BugCommentDTO;
import io.metersphere.bug.mapper.BugCommentMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.notice.constants.NoticeConstants;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BugCommentControllerTests extends BaseTest {

    @Resource
    private BugCommentMapper bugCommentMapper;

    public static final String BUG_COMMENT_GET = "/bug/comment/get";
    public static final String BUG_COMMENT_ADD = "/bug/comment/add";
    public static final String BUG_COMMENT_UPDATE = "/bug/comment/update";
    public static final String BUG_COMMENT_DELETE = "/bug/comment/delete";

    @Test
    @Order(0)
    @Sql(scripts = {"/dml/init_bug_comment.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testGetBugComment() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_COMMENT_GET + "/default-bug-id-for-comment");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        List<BugCommentDTO> comments = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BugCommentDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(comments));
        Assertions.assertTrue(StringUtils.equals("default-bug-comment-id-3", comments.getFirst().getId()));
        // 第二条评论的子评论不为空且ID为default-bug-comment-id-2
        Assertions.assertTrue(CollectionUtils.isNotEmpty(comments.get(1).getChildComments()) &&
                StringUtils.equals("default-bug-comment-id-2", comments.get(1).getChildComments().getFirst().getId()));
    }

    @Test
    @Order(1)
    public void testGetBugEmptyComment() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(BUG_COMMENT_GET + "/default-bug-id-for-comment1");
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        List<BugCommentDTO> comments = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), BugCommentDTO.class);
        // 该缺陷没有评论
        Assertions.assertTrue(CollectionUtils.isEmpty(comments));
    }

    @Test
    @Order(2)
    public void testAddBugCommentOnlySuccess() throws Exception {
        // 只评论, 不@用户, 也不是回复
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setBugId("default-bug-id-for-comment1");
        request.setContent("This is a comment test!");
        request.setEvent(NoticeConstants.Event.COMMENT);
        BugComment bugComment = saveOrUpdateComment(request, false);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a comment test!"));
    }

    @Test
    @Order(3)
    public void testAddBugCommentOnlyError() throws Exception {
        // 只评论, 不@用户, 也不是回复 (缺陷不存在)
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setBugId("default-bug-id-for-comment-x");
        request.setContent("This is a comment test!");
        request.setEvent(NoticeConstants.Event.COMMENT);
        this.requestPost(BUG_COMMENT_ADD, request, status().is5xxServerError());
        // 回复评论, 没有父级评论ID
        request.setReplyUser("oasis-user-id1");
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment test!");
        request.setEvent(NoticeConstants.Event.REPLY);
        this.requestPost(BUG_COMMENT_ADD, request, status().is5xxServerError());
        // 回复评论, 父级评论不存在
        request.setReplyUser("oasis-user-id1");
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment test!");
        request.setParentId("default-bug-comment-id-3-x");
        request.setEvent(NoticeConstants.Event.REPLY);
        this.requestPost(BUG_COMMENT_ADD, request, status().is5xxServerError());
        // 回复评论, 但回复人为空
        request.setReplyUser(null);
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment test!");
        request.setParentId("default-bug-comment-id-3");
        request.setEvent(NoticeConstants.Event.REPLY);
        this.requestPost(BUG_COMMENT_ADD, request, status().is5xxServerError());
    }

    @Test
    @Order(4)
    public void testAddBugCommentWithAtEventSuccess() throws Exception {
        // 评论并@用户, 不是回复
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a at comment test!");
        request.setNotifier(String.join(";", "oasis-user-id4"));
        request.setEvent(NoticeConstants.Event.AT);
        BugComment bugComment = saveOrUpdateComment(request, false);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a at comment test!"));
    }

    @Test
    @Order(5)
    public void testAddBugCommentWithReplyEventSuccess() throws Exception {
        // 评论并回复用户, 但不@
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a reply comment test!");
        request.setReplyUser("oasis-user-id1");
        request.setParentId("default-bug-comment-id-3");
        request.setEvent(NoticeConstants.Event.REPLY);
        BugComment bugComment = saveOrUpdateComment(request, false);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a reply comment test!"));
    }

    @Test
    @Order(6)
    public void testAddBugCommentWithReplyAndAtEventSuccess() throws Exception {
        // 评论并回复用户, 但不@当前回复人
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a reply && at comment test!");
        request.setReplyUser("oasis-user-id");
        request.setNotifier(String.join(";", "oasis-user-id3", "oasis-user-id4"));
        request.setParentId("default-bug-comment-id-2");
        request.setEvent(NoticeConstants.Event.REPLY);
        BugComment bugComment = saveOrUpdateComment(request, false);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a reply && at comment test!"));
        // 评论并回复用户, @当前回复人
        request.setNotifier(String.join(";", "oasis-user-id", "oasis-user-id2"));
        saveOrUpdateComment(request, false);
    }

    @Test
    @Order(7)
    public void testUpdateBugCommentOnlySuccess() throws Exception {
        // 只编辑第一级评论
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setId("default-bug-comment-id-3");
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment test!");
        request.setEvent(NoticeConstants.Event.COMMENT);
        BugComment bugComment = saveOrUpdateComment(request, true);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a comment test!"));
    }

    @Test
    @Order(8)
    public void testUpdateBugCommentWithReplyEventSuccess() throws Exception {
        // 只编辑回复的评论并且带有@用户
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setId("default-bug-comment-id-2");
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment && reply test!");
        request.setReplyUser("admin");
        request.setNotifier(String.join(";", "oasis-user-id1", "oasis-user-id2", "admin"));
        request.setEvent(NoticeConstants.Event.REPLY);
        BugComment bugComment = saveOrUpdateComment(request, true);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a comment && reply test!"));
    }

    @Test
    @Order(9)
    public void testUpdateBugCommentWithAtEventSuccess() throws Exception {
        // 编辑第一级并带有@的评论
        BugCommentEditRequest request = new BugCommentEditRequest();
        request.setId("default-bug-comment-id-3");
        request.setBugId("default-bug-id-for-comment");
        request.setContent("This is a comment && at test!");
        request.setNotifier(String.join(";", "oasis-user-id3", "oasis-user-id4"));
        request.setEvent(NoticeConstants.Event.AT);
        BugComment bugComment = saveOrUpdateComment(request, true);
        BugComment comment = bugCommentMapper.selectByPrimaryKey(bugComment.getId());
        Assertions.assertTrue(StringUtils.equals(comment.getContent(), "This is a comment && at test!"));
    }

    @Test
    @Order(10)
    public void testDeleteBugCommentSuccess() throws Exception {
        // 删除第一级评论
        this.requestGet(BUG_COMMENT_DELETE + "/default-bug-comment-id-1");
        BugComment comment = bugCommentMapper.selectByPrimaryKey("default-bug-comment-id-2");
        Assertions.assertNull(comment);
        // 删除第二级评论
        this.requestGet(BUG_COMMENT_DELETE + "/default-bug-comment-id-4");
        BugComment comment1 = bugCommentMapper.selectByPrimaryKey("default-bug-comment-id-4");
        Assertions.assertNull(comment1);
        // 删除非当前评论人的评论
        this.requestGet(BUG_COMMENT_DELETE + "/default-bug-comment-id-5");
        BugComment comment2 = bugCommentMapper.selectByPrimaryKey("default-bug-comment-id-5");
        Assertions.assertTrue(StringUtils.equals(comment2.getId(), "default-bug-comment-id-5"));
    }

    @Test
    @Order(11)
    public void testDeleteBugCommentError() throws Exception {
        // 评论不存在
        this.requestGet(BUG_COMMENT_DELETE + "/default-bug-comment-id-x");
    }

    private BugComment saveOrUpdateComment(BugCommentEditRequest request, boolean isUpdated) throws Exception{
        MvcResult mvcResult = this.requestPostWithOkAndReturn(isUpdated ? BUG_COMMENT_UPDATE : BUG_COMMENT_ADD, request);
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(sortData, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), BugComment.class);
    }
}
