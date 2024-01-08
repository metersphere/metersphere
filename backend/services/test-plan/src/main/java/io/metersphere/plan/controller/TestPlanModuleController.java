package io.metersphere.plan.controller;

import io.metersphere.plan.dto.request.TestPlanModuleCreateRequest;
import io.metersphere.plan.dto.request.TestPlanModuleUpdateRequest;
import io.metersphere.plan.service.TestPlanModuleService;
import io.metersphere.sdk.constants.HttpMethodConstants;
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

@Tag(name = "测试计划管理-模块树")
@RestController
@RequestMapping("/test-plan/module")
public class TestPlanModuleController {

    @Resource
    private TestPlanModuleService testPlanModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "测试计划管理-模块树-查找模块")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_MODULE_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return testPlanModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "测试计划管理-模块树-添加模块")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_MODULE_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@RequestBody @Validated TestPlanModuleCreateRequest request) {
        return testPlanModuleService.add(request, SessionUtils.getUserId(),"/test-plan/module/add", HttpMethodConstants.POST.name());
    }

    @PostMapping("/update")
    @Operation(summary = "测试计划管理-模块树-修改模块")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_MODULE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "file_module")
    public boolean list(@RequestBody @Validated TestPlanModuleUpdateRequest request) {
        testPlanModuleService.update(request, SessionUtils.getUserId(),"/test-plan/module/update", HttpMethodConstants.POST.name());
        return true;
    }

    @GetMapping("/delete/{deleteId}")
    @Operation(summary = "测试计划管理-模块树-删除模块")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_MODULE_READ_DELETE)
    @CheckOwner(resourceId = "#deleteId", resourceType = "file_module")
    public void deleteNode(@PathVariable String deleteId) {
        testPlanModuleService.deleteModule(deleteId, SessionUtils.getUserId(),"/test-plan/module/delete",HttpMethodConstants.GET.name());
    }

    @PostMapping("/move")
    @Operation(summary = "测试计划管理-模块树-移动模块")
    @RequiresPermissions(PermissionConstants.TEST_PLAN_MODULE_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "test_plan_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        testPlanModuleService.moveNode(request, SessionUtils.getUserId(),"/test-plan/module/move",HttpMethodConstants.POST.name());
    }
}
