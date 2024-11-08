package io.metersphere.functional.controller;

import io.metersphere.functional.request.CaseReviewModuleCreateRequest;
import io.metersphere.functional.request.CaseReviewModuleUpdateRequest;
import io.metersphere.functional.service.CaseReviewModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-用例评审-模块")
@RestController
@RequestMapping("/case/review/module")
public class CaseReviewModuleController {

    @Resource
    private CaseReviewModuleService caseReviewModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "用例管理-用例评审-模块-获取模块树")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return caseReviewModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-用例评审-模块-添加模块")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@RequestBody @Validated CaseReviewModuleCreateRequest request) {
        return caseReviewModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "用例管理-用例评审-模块-修改模块")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "case_review_module")
    public void list(@RequestBody @Validated CaseReviewModuleUpdateRequest request) {
        caseReviewModuleService.update(request, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "用例管理-用例评审-模块-移动模块")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "case_review_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        caseReviewModuleService.moveNode(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{moduleId}")
    @Operation(summary = "用例管理-用例评审-模块-删除模块")
    @RequiresPermissions(PermissionConstants.CASE_REVIEW_READ_DELETE)
    @CheckOwner(resourceId = "#moduleId", resourceType = "case_review_module")
    public void deleteNode(@PathVariable String moduleId) {
        caseReviewModuleService.deleteModule(moduleId, SessionUtils.getUserId());
    }

}
