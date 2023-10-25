package io.metersphere.functional.controller;

import io.metersphere.functional.domain.FunctionalCaseComment;
import io.metersphere.functional.request.FunctionalCaseCommentRequest;
import io.metersphere.functional.service.FunctionalCaseCommentService;
import io.metersphere.functional.service.FunctionalCaseNoticeService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Created;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用例管理-功能用例-用例评论")
@RestController
@RequestMapping("/functional/case/comment")
public class FunctionalCaseCommentController {

    @Resource
    private FunctionalCaseCommentService functionalCaseCommentService;

    @PostMapping("/save")
    @Operation(summary = "用例管理-功能用例-用例评论-创建评论")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_COMMENT_READ_ADD)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.AT,  target = "#targetClass.getRelatedUsers(#functionalCaseCommentRequest)", targetClass = FunctionalCaseNoticeService.class)
    public FunctionalCaseComment saveComment(@Validated({Created.class}) @RequestBody FunctionalCaseCommentRequest functionalCaseCommentRequest) {
        return functionalCaseCommentService.saveComment(functionalCaseCommentRequest, SessionUtils.getUserId());
    }
}
