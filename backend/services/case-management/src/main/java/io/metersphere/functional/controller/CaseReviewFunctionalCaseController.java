package io.metersphere.functional.controller;


import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.ReviewFunctionalCaseDTO;
import io.metersphere.functional.request.BaseReviewCaseBatchRequest;
import io.metersphere.functional.request.CaseReviewFunctionalCasePosRequest;
import io.metersphere.functional.request.BatchReviewFunctionalCaseRequest;
import io.metersphere.functional.request.ReviewFunctionalCasePageRequest;
import io.metersphere.functional.service.CaseReviewFunctionalCaseService;
import io.metersphere.functional.service.CaseReviewLogService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wx
 */
@Tag(name = "用例管理-用例评审-评审列表-评审详情")
@RestController
@RequestMapping("/case/review/detail")
public class CaseReviewFunctionalCaseController {

    @Resource
    private CaseReviewFunctionalCaseService caseReviewFunctionalCaseService;


    @GetMapping("/get-ids/{reviewId}")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-获取已关联用例id集合(关联用例弹窗前调用)")
    @CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public List<String> getCaseIds(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getCaseIdsByReviewId(reviewId);
    }


    @PostMapping("/page")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-已关联用例列表")
    public Pager<List<ReviewFunctionalCaseDTO>> page(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.getViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, caseReviewFunctionalCaseService.page(request, false, userId));
    }


    @PostMapping("/batch/disassociate")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-列表-批量取消关联用例")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.batchDisassociateCaseLog(#request)", msClass = CaseReviewLogService.class)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public void batchDisassociate(@Validated @RequestBody BaseReviewCaseBatchRequest request) {
        caseReviewFunctionalCaseService.disassociate(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-列表-拖拽排序")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void editPos(@Validated @RequestBody CaseReviewFunctionalCasePosRequest request) {
        caseReviewFunctionalCaseService.editPos(request);
    }

    @PostMapping("/batch/review")
    @Operation(summary = "用例管理-功能用例-评审列表-评审详情-列表-批量评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_REVIEW)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public void batchReview(@Validated @RequestBody BatchReviewFunctionalCaseRequest request) {
        caseReviewFunctionalCaseService.batchReview(request, SessionUtils.getUserId());
    }

}
