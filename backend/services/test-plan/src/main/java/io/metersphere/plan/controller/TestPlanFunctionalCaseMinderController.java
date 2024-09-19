package io.metersphere.plan.controller;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.dto.request.BugEditRequest;
import io.metersphere.bug.service.BugLogService;
import io.metersphere.bug.service.BugService;
import io.metersphere.plan.dto.request.TestPlanCaseBatchAddBugRequest;
import io.metersphere.plan.dto.request.TestPlanCaseBatchAssociateBugRequest;
import io.metersphere.plan.service.TestPlanFunctionalCaseMinderService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "测试计划-功能用例-脑图-操作")
@RestController
@RequestMapping("/test-plan/functional/case/minder")
public class TestPlanFunctionalCaseMinderController {

    @Resource
    private BugService bugService;
    @Resource
    private TestPlanFunctionalCaseMinderService testPlanFunctionalCaseMinderService;
    @Resource
    private BugLogService bugLogService;

    @PostMapping("/batch/add-bug")
    @Operation(summary = "测试计划-功能用例-脑图-批量添加缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void minderBatchAddBug(@Validated @RequestPart("request") TestPlanCaseBatchAddBugRequest request,
                                  @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        BugEditRequest bugEditRequest = new BugEditRequest();
        BeanUtils.copyBean(bugEditRequest, request);
        Bug bug = bugService.addOrUpdate(bugEditRequest, files, SessionUtils.getUserId(), SessionUtils.getCurrentOrganizationId(), false);
        bugLogService.minderAddLog(bugEditRequest, files, SessionUtils.getCurrentOrganizationId(), bug.getId(), SessionUtils.getUserId());
        testPlanFunctionalCaseMinderService.minderBatchAssociateBug(request, bug.getId(), SessionUtils.getUserId());
    }

    @PostMapping("/batch/associate-bug")
    @Operation(summary = "测试计划-计划详情-功能用例-脑图-批量关联缺陷")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_READ_EXECUTE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public void batchAssociateBug(@Validated @RequestBody TestPlanCaseBatchAssociateBugRequest request) {
        testPlanFunctionalCaseMinderService.batchAssociateBugByIds(request, SessionUtils.getUserId());
    }
}
