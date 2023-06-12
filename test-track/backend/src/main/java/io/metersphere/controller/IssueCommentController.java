package io.metersphere.controller;

import io.metersphere.base.domain.IssueComment;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.dto.IssueCommentDTO;
import io.metersphere.request.issues.IssuesRelevanceRequest;
import io.metersphere.request.issues.SaveIssueCommentRequest;
import io.metersphere.service.IssueCommentService;
import io.metersphere.service.IssuesService;
import io.metersphere.service.TestCaseCommentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;

@RequestMapping("/issues/comment")
@RestController
public class IssueCommentController {

    @Resource
    private IssueCommentService issueCommentService;

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    @SendNotice(taskType = NoticeConstants.TaskType.DEFECT_TASK, target = "#targetClass.getIssue(#request.issuesId)", targetClass = IssuesService.class,
            event = NoticeConstants.Event.COMMENT, subject = "缺陷")
    @MsRequestLog(module = OperLogModule.TRACK_BUG)
    public IssueComment saveComment(@RequestBody IssuesRelevanceRequest request) {
        request.setId(UUID.randomUUID().toString());
        return issueCommentService.saveComment(request);
    }

    @GetMapping("/list/{issueId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ)
    public List<IssueCommentDTO> getComments(@PathVariable String issueId) {
        return issueCommentService.getComments(issueId);
    }

    @GetMapping("/delete/{commentId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#commentId)", msClass = TestCaseCommentService.class)
    public void deleteComment(@PathVariable String commentId) {
        issueCommentService.delete(commentId);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseCommentService.class)
    public IssueComment editComment(@RequestBody SaveIssueCommentRequest request) {
       return issueCommentService.edit(request);
    }
}
