package io.metersphere.api.controller;

import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.DragApiScenarioModuleRequest;
import io.metersphere.api.service.ApiScenarioModuleService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/api/automation/module")
@RestController
public class ApiScenarioModuleController {

    @Resource
    ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}")
    public List<ApiScenarioModuleDTO> getNodeByProjectId(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return apiScenarioModuleService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/add")
    @MsAuditLog(module = "api_automation", type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public String addNode(@RequestBody ApiScenarioModule node) {
        return apiScenarioModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = "api_automation", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public int editNode(@RequestBody DragApiScenarioModuleRequest node) {
        return apiScenarioModuleService.editNode(node);
    }

    @GetMapping("/list/plan/{planId}")
    public List<ApiScenarioModuleDTO> getNodeByPlanId(@PathVariable String planId) {
        checkPermissionService.checkTestPlanOwner(planId);
        return apiScenarioModuleService.getNodeByPlanId(planId);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "api_automation", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = ApiScenarioModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return apiScenarioModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = "api_automation", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public void dragNode(@RequestBody DragApiScenarioModuleRequest node) {
        apiScenarioModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    public void treeSort(@RequestBody List<String> ids) {
        apiScenarioModuleService.sort(ids);
    }

}
