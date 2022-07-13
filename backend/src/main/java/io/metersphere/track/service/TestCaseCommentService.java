package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseCommentMapper;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseCommentMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReviewReference;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.track.dto.TestCaseCommentDTO;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import io.metersphere.track.request.testreview.TestCaseReviewTestCaseEditRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestCaseCommentService {
    @Resource
    private TestCaseCommentMapper testCaseCommentMapper;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private ExtTestCaseCommentMapper extTestCaseCommentMapper;
    @Resource
    private UserMapper userMapper;

    public TestCaseComment saveComment(SaveCommentRequest request) {
        request.setId(UUID.randomUUID().toString());
        request.setAuthor(SessionUtils.getUser().getId());
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isBlank(request.getType())) {
            request.setType(TestCaseCommentType.CASE.name());
        }
        testCaseCommentMapper.insert(request);
        return request;
    }

    /**
     * 放 TestReviewTestCaseService 通知等会失效
     * 需要走 Spring 的代理对象
     * @param request
     */
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_COMMENT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseCommentService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.REVIEW_TASK, target = "#targetClass.getTestReview(#request.reviewId)", targetClass = TestCaseReviewService.class,
            event = NoticeConstants.Event.COMMENT, subject = "测试评审通知")
    public void saveComment(TestCaseReviewTestCaseEditRequest request) {
        SaveCommentRequest saveCommentRequest = new SaveCommentRequest();
        saveCommentRequest.setCaseId(request.getCaseId());
        saveCommentRequest.setDescription(request.getComment());
        saveCommentRequest.setStatus(request.getStatus());
        saveCommentRequest.setType(TestCaseCommentType.REVIEW.name());
        saveComment(saveCommentRequest);
    }

    public List<TestCaseCommentDTO> getCaseComments(String caseId, String type) {
        return extTestCaseCommentMapper.getCaseComments(caseId, type);
    }

    public List<TestCaseCommentDTO> getCaseComments(String caseId) {
        return this.getCaseComments(caseId, null);
    }

    public void deleteCaseComment(String caseId) {
        TestCaseCommentExample testCaseCommentExample = new TestCaseCommentExample();
        testCaseCommentExample.createCriteria().andCaseIdEqualTo(caseId);
        testCaseCommentMapper.deleteByExample(testCaseCommentExample);
    }

    private String getReviewContext(TestCaseComment testCaseComment, TestCaseWithBLOBs testCaseWithBLOBs) {
        User user = userMapper.selectByPrimaryKey(testCaseComment.getAuthor());
        Long startTime = testCaseComment.getCreateTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = null;
        String sTime = String.valueOf(startTime);
        if (!sTime.equals("null")) {
            start = sdf.format(new Date(Long.parseLong(sTime)));
        }
        String context = "";
        context = "测试评审任务通知：" + user.getName() + "在" + start + "为" + "'" + testCaseWithBLOBs.getName() + "'" + "添加评论:" + testCaseComment.getDescription();
        return context;
    }

    public void delete(String commentId) {
        checkCommentOwner(commentId);
        testCaseCommentMapper.deleteByPrimaryKey(commentId);
    }

    public TestCaseComment edit(SaveCommentRequest request) {
        checkCommentOwner(request.getId());
        testCaseCommentMapper.updateByPrimaryKeySelective(request);
        return testCaseCommentMapper.selectByPrimaryKey(request.getId());
    }

    private void checkCommentOwner(String commentId) {
        TestCaseComment comment = testCaseCommentMapper.selectByPrimaryKey(commentId);
        if (comment == null) {
            MSException.throwException(Translator.get("resource_not_exist"));
        }
        if (!StringUtils.equals(comment.getAuthor(), SessionUtils.getUser().getId())) {
            MSException.throwException(Translator.get("check_owner_comment"));
        }
    }

    public String getLogDetails(String id) {
        TestCaseComment caseComment = testCaseCommentMapper.selectByPrimaryKey(id);
        if (caseComment != null) {
            TestCase review = testCaseMapper.selectByPrimaryKey(caseComment.getCaseId());
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(caseComment, TestCaseReviewReference.commentReviewColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(caseComment.getId()), review.getProjectId(), caseComment.getDescription(), caseComment.getAuthor(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
