package io.metersphere.api.controller.debug;

import io.metersphere.api.dto.debug.ApiDebugRequest;
import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.service.debug.ApiDebugModuleService;
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
import java.util.Map;

@Tag(name = "接口测试-接口调试-模块")
@RestController
@RequestMapping("/api/debug/module")
public class ApiDebugModuleController {

    @Resource
    private ApiDebugModuleService apiDebugModuleService;

    @GetMapping("/tree")
    @Operation(summary = "接口测试-接口调试-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_READ)
    public List<BaseTreeNode> getTree() {
        return apiDebugModuleService.getTree(SessionUtils.getUserId());
    }

    @PostMapping("/add")
    @Operation(summary = "接口测试-接口调试-模块-添加模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_ADD)
    public String add(@RequestBody @Validated ModuleCreateRequest request) {
        return apiDebugModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "接口测试-接口调试-模块-修改模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_UPDATE)
    public boolean update(@RequestBody @Validated ModuleUpdateRequest request) {
        apiDebugModuleService.update(request, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
        return true;
    }

    @GetMapping("/delete/{deleteId}")
    @Operation(summary = "接口测试-接口调试-模块-删除模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_DELETE)
    public void deleteNode(@PathVariable String deleteId) {
        apiDebugModuleService.deleteModule(deleteId, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "接口测试-接口调试-模块-移动模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_UPDATE)
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        apiDebugModuleService.moveNode(request, SessionUtils.getUserId());
    }

    @PostMapping("/count")
    @Operation(summary = "接口测试-接口调试-模块-统计模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEBUG_READ)
    public Map<String, Long> moduleCount(@Validated @RequestBody ApiDebugRequest request) {
        return apiDebugModuleService.moduleCount(request, SessionUtils.getUserId());
    }
}
