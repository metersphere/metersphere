package io.metersphere.functional.controller;

import io.metersphere.functional.request.ReviewFunctionalCaseRequest;
import io.metersphere.functional.service.ReviewFunctionalCaseService;
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

@Tag(name = "用例管理-用例评审-评审功能用例")
@RestController
@RequestMapping("/review/functional/case")
public class ReviewFunctionalCaseController {

    @Resource
    private ReviewFunctionalCaseService reviewFunctionalCaseService;

    @PostMapping("/save")
    @Operation(summary = "用例管理-用例评审-评审功能用例-提交评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    public void saveReview(@Validated @RequestBody ReviewFunctionalCaseRequest request) {
        reviewFunctionalCaseService.saveReview(request, SessionUtils.getUserId());
    }


}
