package io.metersphere.functional.controller;

import io.metersphere.functional.request.FunctionalCaseModuleCreateRequest;
import io.metersphere.functional.request.FunctionalCaseModuleUpdateRequest;
import io.metersphere.functional.service.FunctionalCaseModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用例管理-功能用例-模块")
@RestController
@RequestMapping("/functional/case/module")
public class FunctionalCaseModuleController {

    @Resource
    private FunctionalCaseModuleService functionalCaseModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "用例管理-功能用例-模块-获取模块树")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ)
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return functionalCaseModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "用例管理-功能用例-模块-添加模块")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_ADD)
    public void add(@RequestBody @Validated FunctionalCaseModuleCreateRequest request) {
        functionalCaseModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "用例管理-功能用例-模块-修改模块")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void list(@RequestBody @Validated FunctionalCaseModuleUpdateRequest request) {
        functionalCaseModuleService.update(request, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "用例管理-功能用例-模块-移动模块")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_UPDATE)
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        functionalCaseModuleService.moveNode(request, SessionUtils.getUserId());
    }

    @GetMapping("/delete/{moduleId}")
    @Operation(summary = "用例管理-功能用例-模块-删除模块")
    @RequiresPermissions(PermissionConstants.FUNCTIONAL_CASE_READ_DELETE)
    public void deleteNode(@PathVariable String moduleId) {
        functionalCaseModuleService.deleteModule(moduleId, SessionUtils.getUserId());
    }
}
