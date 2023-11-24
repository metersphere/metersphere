package io.metersphere.functional.controller;

import io.metersphere.functional.request.CaseReviewRequest;
import io.metersphere.functional.request.CaseReviewFollowerRequest;
import io.metersphere.functional.service.CaseReviewLogService;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.domain.User;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-用例评审")
@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-创建用例评审")
    @Log(type = OperationLogType.ADD, expression = "#msClass.addCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_ADD)
    public void addCaseReview(@Validated @RequestBody CaseReviewRequest request) {
        caseReviewService.addCaseReview(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit")
    @Operation(summary = "用例管理-用例评审-编辑用例评审")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.addCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editCaseReview(@Validated({Updated.class}) @RequestBody CaseReviewRequest request) {
        caseReviewService.editCaseReview(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-用例评审-关注/取消关注用例")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editFollower(@Validated @RequestBody CaseReviewFollowerRequest request) {
        caseReviewService.editFollower(request.getCaseReviewId(), SessionUtils.getUserId());
    }

    @GetMapping("/user-option/{projectId}")
    @Operation(summary = "用例管理-用例评审-获取具有评审权限的用户")
    @RequiresPermissions(value = {PermissionConstants.CASE_REVIEW_READ_ADD,PermissionConstants.CASE_REVIEW_READ_UPDATE}, logical = Logical.OR)
    public List<User> getReviewUserList(@PathVariable String projectId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
                                  @RequestParam(value = "keyword", required = false) String keyword) {
        return caseReviewService.getReviewUserList(projectId, keyword);
    }

}