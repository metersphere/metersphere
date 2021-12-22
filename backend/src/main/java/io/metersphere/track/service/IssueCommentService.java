package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.IssueComment;
import io.metersphere.base.domain.Issues;
import io.metersphere.base.mapper.IssueCommentMapper;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.ext.ExtIssueCommentMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.track.TestCaseReviewReference;
import io.metersphere.track.dto.IssueCommentDTO;
import io.metersphere.track.request.issues.IssuesRelevanceRequest;
import io.metersphere.track.request.issues.SaveIssueCommentRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class IssueCommentService {
    @Resource
    private IssuesMapper issuesMapper;
    @Resource
    private ExtIssueCommentMapper extIssueCommentMapper;
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
