package io.metersphere.functional.controller;

import com.alibaba.excel.util.StringUtils;
import io.metersphere.functional.dto.FunctionalMinderTreeDTO;
import io.metersphere.functional.dto.MinderOptionDTO;
import io.metersphere.functional.request.FunctionalCaseMindRequest;
import io.metersphere.functional.request.FunctionalCaseMinderEditRequest;
import io.metersphere.functional.request.FunctionalCaseMinderRemoveRequest;
import io.metersphere.functional.request.FunctionalCaseReviewMindRequest;
import io.metersphere.functional.service.FunctionalCaseLogService;
import io.metersphere.functional.service.FunctionalCaseMinderService;
import io.metersphere.functional.service.FunctionalCaseNoticeService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.annotation.SendNotice;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author guoyuqi
 */
@Tag(name = "用例管理-功能用例-脑图")
@RestController
@RequestMapping("/functional/mind/case")
public class FunctionalCaseMinderController {

    @Resource
    private FunctionalCaseMinderService functionalCaseMinderService;

    @PostMapping("/list")
    @Operation(summary = "用例管理-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public List<FunctionalMinderTreeDTO> getFunctionalCaseMinderTree(@Validated @RequestBody FunctionalCaseMindRequest request) {
        return functionalCaseMinderService.getMindFunctionalCase(request, false);
    }

    @PostMapping("/update/source/name")
    @Operation(summary = "脑图更新资源名称")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateMinderFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainFunctionalCaseMinderDTO(#request)", targetClass = FunctionalCaseNoticeService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "functional_case")
    public void updateFunctionalCaseName(@Validated @RequestBody FunctionalCaseMinderEditRequest request) {
        String userId = SessionUtils.getUserId();
         functionalCaseMinderService.updateFunctionalCase(request, userId);
    }

    @PostMapping("/update/source/priority")
    @Operation(summary = "脑图更新用例等级")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.updateMinderFunctionalCaseLog(#request)", msClass = FunctionalCaseLogService.class)
    @SendNotice(taskType = NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK, event = NoticeConstants.Event.UPDATE, target = "#targetClass.getMainFunctionalCaseMinderDTO(#request)", targetClass = FunctionalCaseNoticeService.class)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "functional_case")
    public void updateFunctionalCasePriority(@Validated @RequestBody FunctionalCaseMinderEditRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseMinderService.updateFunctionalCase(request, userId);
    }

    @PostMapping("/batch/remove")
    @Operation(summary = "脑图批量移动（移动到某个节点下或者移动排序）")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void removeFunctionalCaseBatch(@Validated @RequestBody FunctionalCaseMinderRemoveRequest request) {
        String userId = SessionUtils.getUserId();
        functionalCaseMinderService.removeFunctionalCaseBatch(request, userId);
    }

    @PostMapping("/batch/delete/{projectId}")
    @Operation(summary = "脑图批量删除")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteBatchMinderFunctionalCaseLog(#resourceList)", msClass = FunctionalCaseLogService.class)
    public void deleteFunctionalCaseBatch(@PathVariable String projectId, @Validated @RequestBody @Schema(description = "节点和节点类型的集合", requiredMode = Schema.RequiredMode.REQUIRED) List<MinderOptionDTO> resourceList) {
        String userId = SessionUtils.getUserId();
        functionalCaseMinderService.deleteFunctionalCaseBatch(projectId, resourceList, userId);
    }


    @PostMapping("/review/list")
    @Operation(summary = "用例管理-功能用例-脑图用例跟根据模块ID查询列表")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_MINDER)
    @CheckOwner(resourceId = "#request.getReviewId()", resourceType = "case_review")
    public List<FunctionalMinderTreeDTO> getReviewMindFunctionalCase(@Validated @RequestBody FunctionalCaseReviewMindRequest request) {
        String userId = StringUtils.EMPTY;
        if (request.isViewFlag()) {
            userId = SessionUtils.getUserId();
        }
        String viewStatusUserId = StringUtils.EMPTY;
        if (request.isViewStatusFlag()) {
            viewStatusUserId = SessionUtils.getUserId();
        }
        return functionalCaseMinderService.getReviewMindFunctionalCase(request, false, userId, viewStatusUserId);
    }

}
