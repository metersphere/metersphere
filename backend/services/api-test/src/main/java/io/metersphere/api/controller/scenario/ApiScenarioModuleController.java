package io.metersphere.api.controller.scenario;

import io.metersphere.api.dto.debug.ModuleCreateRequest;
import io.metersphere.api.dto.debug.ModuleUpdateRequest;
import io.metersphere.api.dto.scenario.ApiScenarioModuleRequest;
import io.metersphere.api.service.scenario.ApiScenarioModuleService;
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
import java.util.Map;

@Tag(name = "接口测试-接口场景-模块")
@RestController
@RequestMapping("/api/scenario/module")
public class ApiScenarioModuleController {

    @Resource
    private ApiScenarioModuleService apiScenarioModuleService;

    @PostMapping("/tree")
    @Operation(summary = "接口测试-接口场景-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@RequestBody @Validated ApiScenarioModuleRequest request) {
        return apiScenarioModuleService.getTree(request);
    }

    @PostMapping("/add")
    @Operation(summary = "接口测试-接口场景-模块-添加模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_ADD)
    public String add(@RequestBody @Validated ModuleCreateRequest request) {
        return apiScenarioModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "接口测试-接口场景-模块-修改模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.id", resourceType = "api_scenario_module")
    public boolean update(@RequestBody @Validated ModuleUpdateRequest request) {
        apiScenarioModuleService.update(request, SessionUtils.getUserId());
        return true;
    }

    @GetMapping("/delete/{id}")
    @Operation(summary = "接口测试-接口场景-模块-删除模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_DELETE)
    @CheckOwner(resourceId = "#id", resourceType = "api_scenario_module")
    public void deleteNode(@PathVariable String id) {
        apiScenarioModuleService.deleteModule(id, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "接口测试-接口场景-模块-移动模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_UPDATE)
    @CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "api_scenario_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        apiScenarioModuleService.moveNode(request, SessionUtils.getUserId());
    }

    @PostMapping("/count")
    @Operation(summary = "接口测试-接口场景-模块-统计模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody ApiScenarioModuleRequest request) {
        return apiScenarioModuleService.moduleCount(request, false);
    }

    @PostMapping("/trash/count")
    @Operation(summary = "接口测试-接口场景-模块-统计回收站模块数量")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public Map<String, Long> moduleCountTrash(@Validated @RequestBody ApiScenarioModuleRequest request) {
        return apiScenarioModuleService.moduleCount(request, true);
    }

    @PostMapping("/trash/tree")
    @Operation(summary = "接口测试-接口场景-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_API_SCENARIO_READ)
    @CheckOwner(resourceId = "#request.projectId", resourceType = "project")
    public List<BaseTreeNode> getTrashTree(@RequestBody @Validated ApiScenarioModuleRequest request) {
        return apiScenarioModuleService.getTrashTree(request);
    }
}
