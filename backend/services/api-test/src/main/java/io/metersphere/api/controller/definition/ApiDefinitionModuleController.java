package io.metersphere.api.controller.definition;

import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.api.dto.definition.EnvApiModuleRequest;
import io.metersphere.api.dto.definition.EnvApiTreeDTO;
import io.metersphere.api.service.definition.ApiDefinitionModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "接口测试-接口管理-模块")
@RestController
@RequestMapping("/api/definition/module")
public class ApiDefinitionModuleController {

    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;

    @PostMapping("/tree")
    @Operation(summary = "接口测试-接口管理-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTreeAndRequest(@RequestBody @Validated ApiModuleRequest request) {
        return apiDefinitionModuleService.getTree(request, false, true);
    }

    @PostMapping("/add")
    @Operation(summary = "接口测试-接口管理-模块-添加模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_ADD)
    public String add(@RequestBody @Validated ModuleCreateRequest request) {
        return apiDefinitionModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "接口测试-接口管理-模块-修改模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.id", resourceType = "api_definition_module")
    public boolean update(@RequestBody @Validated ModuleUpdateRequest request) {
        apiDefinitionModuleService.update(request, SessionUtils.getUserId());
        return true;
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口管理-模块-删除模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "api_definition_module")
    public void deleteNode(@PathVariable String id) {
        apiDefinitionModuleService.deleteModule(id, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "接口测试-接口管理-模块-移动模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_UPDATE)
    @CheckOwner(resourceId = "#request.dragNodeId", resourceType = "api_definition_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        apiDefinitionModuleService.moveNode(request, SessionUtils.getUserId());
    }

    @PostMapping("/count")
    @Operation(summary = "接口测试-接口管理-模块-统计模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody ApiModuleRequest request) {
        return apiDefinitionModuleService.moduleCount(request, false);
    }

    @PostMapping("/trash/count")
    @Operation(summary = "接口测试-接口管理-模块-统计回收站模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCountTrash(@Validated @RequestBody ApiModuleRequest request) {
        return apiDefinitionModuleService.moduleCount(request, true);
    }

    @PostMapping("/trash/tree")
    @Operation(summary = "接口测试-接口管理-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTrashTree(@RequestBody @Validated ApiModuleRequest request) {
        return apiDefinitionModuleService.getTrashTree(request);
    }

    @PostMapping("/env/tree")
    @Operation(summary = "获取环境中的接口树和选中的模块")
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_READ, PermissionConstants.PROJECT_ENVIRONMENT_READ}, logical = Logical.OR)
    public EnvApiTreeDTO envTree(@RequestBody @Validated EnvApiModuleRequest request) {
        return apiDefinitionModuleService.envTree(request);
    }

    @PostMapping("/only/tree")
    @Operation(summary = "接口测试-接口管理-模块-不包含请求数据的模块树")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@RequestBody @Validated ApiModuleRequest request) {
        return apiDefinitionModuleService.getTree(request, false, false);
    }
}
