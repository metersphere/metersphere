package io.metersphere.api.controller;

import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.DragApiScenarioModuleRequest;
import io.metersphere.api.service.ApiScenarioModuleService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.service.CheckOwnerService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.annotation.Resource;

@RequestMapping("/api/automation/module")
@RestController
@RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER, RoleConstants.ORG_ADMIN}, logical = Logical.OR)
public class ApiScenarioModuleController {

    @Resource
    ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private CheckOwnerService checkOwnerService;

    @GetMapping("/list/{projectId}")
    public List<ApiScenarioModuleDTO> getNodeByProjectId(@PathVariable String projectId) {
        checkOwnerService.checkProjectOwner(projectId);
        return apiScenarioModuleService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/add")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public String addNode(@RequestBody ApiScenarioModule node) {
        return apiScenarioModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int editNode(@RequestBody DragApiScenarioModuleRequest node) {
        return apiScenarioModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return apiScenarioModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresRoles(value = {RoleConstants.TEST_USER, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void dragNode(@RequestBody DragApiScenarioModuleRequest node) {
        apiScenarioModuleService.dragNode(node);
    }
}
