package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtIssueCommentMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseCommentMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReviewReference;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.dto.IssueCommentDTO;
import io.metersphere.track.dto.TestCaseCommentDTO;
import io.metersphere.track.request.issues.IssuesRelevanceRequest;
import io.metersphere.track.request.issues.SaveIssueCommentRequest;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueCommentService {
    @Resource
    private TestCaseCommentMapper testCaseCommentMapper;
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private ExtIssueCommentMapper extIssueCommentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private IssueCommentMapper issueCommentMapper;

    public IssueComment saveComment(IssuesRelevanceRequest request) {
        IssueComment issueComment = new IssueComment();
        issueComment.setId(request.getId());
        issueComment.setAuthor(SessionUtils.getUser().getId());
        issueComment.setIssueId(request.getIssuesId());
        issueComment.setCreateTime(System.currentTimeMillis());
        issueComment.setUpdateTime(System.currentTimeMillis());
        issueComment.setDescription(request.getDescription());
        issueCommentMapper.insert(issueComment);
        return issueComment;
    }

    public List<IssueCommentDTO> getComments(String caseId) {
        return extIssueCommentMapper.getComments(caseId);
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
        issueCommentMapper.deleteByPrimaryKey(commentId);
    }

    public IssueComment edit(SaveIssueCommentRequest request) {
        checkCommentOwner(request.getId());
        issueCommentMapper.updateByPrimaryKeySelective(request);
        return issueCommentMapper.selectByPrimaryKey(request.getId());
    }

    private void checkCommentOwner(String commentId) {
        IssueComment comment = issueCommentMapper.selectByPrimaryKey(commentId);
        if (!StringUtils.equals(comment.getAuthor(), SessionUtils.getUser().getId())) {
            MSException.throwException(Translator.get("check_owner_comment"));
        }
    }

    public String getLogDetails(String id) {
        IssueComment issueComment = issueCommentMapper.selectByPrimaryKey(id);
        if (issueComment != null) {
            Issues review = issuesMapper.selectByPrimaryKey(issueComment.getIssueId());
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(issueComment, TestCaseReviewReference.commentReviewColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(issueComment.getId()), review.getProjectId(), issueComment.getDescription(), issueComment.getAuthor(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
