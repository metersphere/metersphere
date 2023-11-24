package io.metersphere.functional.controller;

import io.metersphere.functional.request.CaseReviewAddRequest;
import io.metersphere.functional.request.CaseReviewFollowerRequest;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用例管理-用例评审")
@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-创建用例评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_ADD)
    public void addCaseReview(@Validated @RequestBody CaseReviewAddRequest request) {
        caseReviewService.addCaseReview(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-用例评审-关注/取消关注用例")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void editFollower(@Validated @RequestBody CaseReviewFollowerRequest request) {
        caseReviewService.editFollower(request.getCaseReviewId(), SessionUtils.getUserId());
    }

}