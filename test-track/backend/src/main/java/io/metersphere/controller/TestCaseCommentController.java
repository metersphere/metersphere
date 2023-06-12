package io.metersphere.controller;

import io.metersphere.base.domain.TestCaseComment;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.notice.annotation.SendNotice;
import io.metersphere.dto.TestCaseCommentDTO;
import io.metersphere.request.testreview.SaveCommentRequest;
import io.metersphere.service.TestCaseCommentService;
import io.metersphere.service.TestCaseService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/test/case/comment")
@RestController
public class TestCaseCommentController {

    @Resource
    private TestCaseCommentService testCaseCommentService;

    @PostMapping("/save")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_COMMENT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseCommentService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.TRACK_TEST_CASE_TASK, target = "#targetClass.getTestCase(#request.caseId)", targetClass = TestCaseService.class,
            event = NoticeConstants.Event.COMMENT, subject = "测试用例通知")
    public TestCaseComment saveComment(@RequestBody SaveCommentRequest request) {
        return testCaseCommentService.saveComment(request);
    }

    @GetMapping("/list/{caseId}/{type}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ,
            PermissionConstants.PROJECT_TRACK_REVIEW_READ,  PermissionConstants.PROJECT_TRACK_PLAN_READ}, logical = Logical.OR)
    public List<TestCaseCommentDTO> getCaseComments(@PathVariable String caseId, @PathVariable String type) {
        return testCaseCommentService.getCaseComments(caseId, type);
    }

    @GetMapping("/list/{caseId}/{type}/{belongId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ,
            PermissionConstants.PROJECT_TRACK_REVIEW_READ,  PermissionConstants.PROJECT_TRACK_PLAN_READ}, logical = Logical.OR)
    public List<TestCaseCommentDTO> getCaseComments(@PathVariable String caseId, @PathVariable String type, @PathVariable String belongId) {
        return testCaseCommentService.getCaseComments(caseId, type, belongId);
    }

    @GetMapping("/list/{caseId}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ,
            PermissionConstants.PROJECT_TRACK_REVIEW_READ,  PermissionConstants.PROJECT_TRACK_PLAN_READ}, logical = Logical.OR)
    public List<TestCaseCommentDTO> getCaseComments(@PathVariable String caseId) {
        return testCaseCommentService.getCaseComments(caseId, null);
    }

    @GetMapping("/delete/{commentId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_COMMENT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#commentId)", msClass = TestCaseCommentService.class)
    public void deleteComment(@PathVariable String commentId) {
        testCaseCommentService.delete(commentId);
    }

    @PostMapping("/edit")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_REVIEW_READ_COMMENT)
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE_REVIEW, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.id)", content = "#msClass.getLogDetails(#request.id)", msClass = TestCaseCommentService.class)
    public TestCaseComment editComment(@RequestBody SaveCommentRequest request) {
       return testCaseCommentService.edit(request);
    }
}
