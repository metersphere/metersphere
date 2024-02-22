package io.metersphere.functional.controller;


import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.dto.ReviewFunctionalCaseDTO;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.CaseReviewFunctionalCaseService;
import io.metersphere.functional.service.CaseReviewLogService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-获取已关联用例id集合(关联用例弹窗前调用)")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_RELEVANCE)
    @CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public List<String> getCaseIds(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getCaseIdsByReviewId(reviewId);
    }


    @PostMapping("/page")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例列表")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    public Pager<List<ReviewFunctionalCaseDTO>> page(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.isViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        String viewStatusUserId = StringUtils.EMPTY;
        if (request.isViewStatusFlag()) {
            viewStatusUserId = SessionUtils.getUserId();
        }

        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize());
        return PageUtils.setPageInfo(page, caseReviewFunctionalCaseService.page(request, false, userId, viewStatusUserId));
    }


    @GetMapping("/tree/{reviewId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例列表模块树")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public List<BaseTreeNode> getTree(@PathVariable String reviewId) {
        return caseReviewFunctionalCaseService.getTree(reviewId);
    }


    @PostMapping("/module/count")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-已关联用例统计模块数量")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody ReviewFunctionalCasePageRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.isViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        return caseReviewFunctionalCaseService.moduleCount(request, false, userId);
    }


    @PostMapping("/batch/disassociate")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量取消关联用例")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_RELEVANCE)
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.batchDisassociateCaseLog(#request)", msClass = CaseReviewLogService.class)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public void batchDisassociate(@Validated @RequestBody BaseReviewCaseBatchRequest request) {
        caseReviewFunctionalCaseService.disassociate(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-拖拽排序")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void editPos(@Validated @RequestBody CaseReviewFunctionalCasePosRequest request) {
        caseReviewFunctionalCaseService.editPos(request);
    }

    @PostMapping("/batch/review")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量评审")
    @RequiresPermissions(value = {PermissionConstants.CASE_REVIEW_REVIEW, PermissionConstants.CASE_REVIEW_READ_UPDATE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public void batchReview(@Validated @RequestBody BatchReviewFunctionalCaseRequest request) {
        caseReviewFunctionalCaseService.batchReview(request, SessionUtils.getUserId());
    }


    @PostMapping("/batch/edit/reviewers")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-列表-批量修改评审人")
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    public void batchEditReviewUser(@Validated @RequestBody BatchEditReviewerRequest request) {
        caseReviewFunctionalCaseService.batchEditReviewUser(request, SessionUtils.getUserId());
    }

    @GetMapping("/reviewer/status/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-评审列表-评审详情-评审结果的气泡数据")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<OptionDTO> getUserStatus(@Schema(description = "评审id", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable("reviewId") String reviewId, @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
                                         @PathVariable("caseId") String caseId) {
        return caseReviewFunctionalCaseService.getUserStatus(reviewId, caseId);
    }


}
