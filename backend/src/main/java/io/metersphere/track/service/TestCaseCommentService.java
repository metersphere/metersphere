package io.metersphere.track.service;

import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.base.domain.TestCaseCommentExample;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.TestCaseCommentMapper;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseReviewMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.service.DingTaskService;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import io.metersphere.notice.service.WxChatTaskService;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseCommentService {

    @Resource
    TestCaseCommentMapper testCaseCommentMapper;
    @Resource
    private TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    MailService mailService;
    @Resource
    TestCaseMapper testCaseMapper;
    @Resource
    DingTaskService dingTaskService;
    @Resource
    WxChatTaskService wxChatTaskService;
    @Resource
    NoticeService noticeService;


    public void saveComment(SaveCommentRequest request) {
        TestCaseComment testCaseComment = new TestCaseComment();
        testCaseComment.setId(UUID.randomUUID().toString());
        testCaseComment.setAuthor(SessionUtils.getUser().getId());
        testCaseComment.setCaseId(request.getCaseId());
        testCaseComment.setCreateTime(System.currentTimeMillis());
        testCaseComment.setUpdateTime(System.currentTimeMillis());
        testCaseComment.setDescription(request.getDescription());
        testCaseCommentMapper.insert(testCaseComment);
        TestCaseWithBLOBs testCaseWithBLOBs;
        testCaseWithBLOBs = testCaseMapper.selectByPrimaryKey(request.getCaseId());
        List<String> userIds = new ArrayList<>();
        userIds.add(testCaseWithBLOBs.getMaintainer());//用例维护人
        try {
            String context = getReviewContext(testCaseComment, testCaseWithBLOBs);
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            List<MessageDetail> taskList = messageSettingDetail.getReviewTask();
            taskList.forEach(r -> {
                switch (r.getType()) {
                    case NoticeConstants.NAIL_ROBOT:
                        dingTaskService.sendNailRobot(r, userIds, context, NoticeConstants.COMMENT);
                        break;
                    case NoticeConstants.WECHAT_ROBOT:
                        wxChatTaskService.sendWechatRobot(r, userIds, context, NoticeConstants.COMMENT);
                        break;
                    case NoticeConstants.EMAIL:
                        mailService.sendCommentNotice(r, userIds, request, testCaseWithBLOBs, NoticeConstants.COMMENT);
                        break;
                }
            });
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    public List<TestCaseComment> getComments(String caseId) {
        TestCaseCommentExample testCaseCommentExample = new TestCaseCommentExample();
        testCaseCommentExample.setOrderByClause("update_time desc");
        testCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        List<TestCaseComment> testCaseComments = testCaseCommentMapper.selectByExampleWithBLOBs(testCaseCommentExample);
        testCaseComments.forEach(testCaseComment -> {
            String authorId = testCaseComment.getAuthor();
            User user = userMapper.selectByPrimaryKey(authorId);
            String author = user == null ? authorId : user.getName();
            testCaseComment.setAuthor(author);
        });
        return testCaseComments;
    }

    public void deleteComment(String caseId) {
        TestCaseCommentExample testCaseCommentExample = new TestCaseCommentExample();
        testCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        testCaseCommentMapper.deleteByExample(testCaseCommentExample);
    }

    private String getReviewContext(TestCaseComment testCaseComment, TestCaseWithBLOBs testCaseWithBLOBs) {
        Long startTime = testCaseComment.getCreateTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String context = "";
        context = "测试评审任务通知：" + testCaseComment.getAuthor() + "在" + start + "为" + "'" + testCaseWithBLOBs.getName() + "'" + "添加评论:" + testCaseComment.getDescription();
        return context;
    }
}
