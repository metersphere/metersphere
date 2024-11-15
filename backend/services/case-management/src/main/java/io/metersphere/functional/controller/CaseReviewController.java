package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.functional.domain.CaseReview;
import io.metersphere.functional.dto.CaseReviewDTO;
import io.metersphere.functional.request.*;
import io.metersphere.functional.service.CaseReviewLogService;
import io.metersphere.functional.service.CaseReviewNoticeService;
import io.metersphere.functional.service.CaseReviewService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.dto.user.UserDTO;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.PageUtils;
import io.metersphere.system.utils.Pager;
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
import java.util.Map;

@Tag(name = "用例管理-用例评审")
@RestController
@RequestMapping("/case/review")
public class CaseReviewController {

    @Resource
    private CaseReviewService caseReviewService;

    @PostMapping("/page")
    @Operation(summary = "用例管理-用例评审-用例列表查询")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<CaseReviewDTO>> getFunctionalCasePage(@Validated @RequestBody CaseReviewPageRequest request) {
        Page<Object> page = PageHelper.startPage(request.getCurrent(), request.getPageSize(),
                StringUtils.isNotBlank(request.getSortString()) ? request.getSortString() : "pos desc");
        return PageUtils.setPageInfo(page, caseReviewService.getCaseReviewPage(request));
    }

    @PostMapping("/module/count")
    @Operation(summary = "用例管理-用例评审-统计模块数量")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody CaseReviewPageRequest request) {
        return caseReviewService.moduleCount(request);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-创建用例评审")
    @Log(type = OperationLogType.ADD, expression = "#msClass.addCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public CaseReview addCaseReview(@Validated @RequestBody CaseReviewRequest request) {
        return caseReviewService.addCaseReview(request, SessionUtils.getUserId());
    }

    @PostMapping("/copy")
    @Operation(summary = "用例管理-用例评审-复制用例评审")
    @Log(type = OperationLogType.ADD, expression = "#msClass.copyCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.CREATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public CaseReview copyCaseReview(@Validated @RequestBody CaseReviewCopyRequest request) {
        return caseReviewService.copyCaseReview(request, SessionUtils.getUserId());
    }

    @PostMapping("/edit")
    @Operation(summary = "用例管理-用例评审-编辑用例评审")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainCaseReview(#request)", targetClass = CaseReviewNoticeService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "case_review")
    public void editCaseReview(@Validated({Updated.class}) @RequestBody CaseReviewRequest request) {
        caseReviewService.editCaseReview(request, SessionUtils.getUserId());
    }

    @GetMapping("/user-option/{projectId}")
    @Operation(summary = "用例管理-用例评审-获取具有评审权限的用户")
    @RequiresPermissions(value = {PermissionConstants.CASE_REVIEW_READ, PermissionConstants.CASE_REVIEW_READ_ADD, PermissionConstants.CASE_REVIEW_READ_UPDATE}, logical = Logical.OR)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<UserDTO> getReviewUserList(@PathVariable String projectId, @Schema(description = "查询关键字，根据邮箱和用户名查询")
    @RequestParam(value = "keyword", required = false) String keyword) {
        return caseReviewService.getReviewUserList(projectId, keyword);
    }

    @PostMapping("/edit/follower")
    @Operation(summary = "用例管理-用例评审-关注/取消关注用例")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getCaseReviewId()", resourceType = "case_review")
    public void editFollower(@Validated @RequestBody CaseReviewFollowerRequest request) {
        caseReviewService.editFollower(request.getCaseReviewId(), SessionUtils.getUserId());
    }

    @PostMapping("/associate")
    @Operation(summary = "用例管理-用例评审-关联用例")
    @Log(type = OperationLogType.ASSOCIATE, expression = "#msClass.associateCaseLog(#request)", msClass = CaseReviewLogService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_RELEVANCE)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public void associateCase(@Validated @RequestBody CaseReviewAssociateRequest request) {
        caseReviewService.associateCase(request, SessionUtils.getUserId());
    }

    @GetMapping("/disassociate/{reviewId}/{caseId}")
    @Operation(summary = "用例管理-用例评审-取消关联用例")
    @Log(type = OperationLogType.DISASSOCIATE, expression = "#msClass.disAssociateCaseLog(#reviewId, #caseId)", msClass = CaseReviewLogService.class)
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_RELEVANCE)
    @CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public void disassociate(@PathVariable String reviewId, @PathVariable String caseId) {
        caseReviewService.disassociate(reviewId, caseId, SessionUtils.getUserId());
    }

    @PostMapping("/edit/pos")
    @Operation(summary = "用例管理-用例评审-拖拽排序")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateCaseReviewLogByPos(#request)", msClass = CaseReviewLogService.class)
    public void editPos(@Validated @RequestBody PosRequest request) {
        caseReviewService.editPos(request);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "用例管理-用例评审-查看评审详情")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#id", resourceType = "case_review")
    public CaseReviewDTO getCaseReviewDetail(@PathVariable String id) {
        return caseReviewService.getCaseReviewDetail(id, SessionUtils.getUserId());
    }

    @PostMapping("/batch/move")
    @Operation(summary = "用例管理-用例评审-批量移动用例评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "case_review")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateBatchCaseReviewLog(#request)", msClass = CaseReviewLogService.class)
    public void batchMoveCaseReview(@Validated @RequestBody CaseReviewBatchRequest request) {
        caseReviewService.batchMoveCaseReview(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{projectId}/{reviewId}")
    @Operation(summary = "用例管理-用例评审-删除用例评审")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_DELETE)
    @SendNotice(taskType = NoticeConstants.TaskType.CASE_REVIEW_TASK, event = NoticeConstants.Event.DELETE, target = "#targetClass.getMainCaseReview(#reviewId)", targetClass = CaseReviewNoticeService.class)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteFunctionalCaseLog(#reviewId)", msClass = CaseReviewLogService.class)
    @CheckOwner(resourceId = "#reviewId", resourceType = "case_review")
    public void deleteCaseReview(@PathVariable String reviewId, @PathVariable String projectId) {
        caseReviewService.deleteCaseReview(reviewId, projectId);
    }
}