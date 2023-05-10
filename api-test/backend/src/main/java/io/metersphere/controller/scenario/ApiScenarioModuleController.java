package io.metersphere.controller.scenario;

import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.DragApiScenarioModuleRequest;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.scenario.ApiScenarioModuleService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RequestMapping("/api/automation/module")
@RestController
public class ApiScenarioModuleController {

    @Resource
    ApiScenarioModuleService apiScenarioModuleService;

    @GetMapping("/list/{projectId}")
    public List<ApiScenarioModuleDTO> getNodeByProjectId(@PathVariable String projectId) {
        return apiScenarioModuleService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/list/{projectId}")
    public List<ApiScenarioModuleDTO> searchNodeByProjectId(@PathVariable String projectId, @RequestBody ApiScenarioRequest request) {
        return apiScenarioModuleService.getNodeTreeByProjectId(projectId, request);
    }

    @GetMapping("/trash/list/{projectId}")
    public List<ApiScenarioModuleDTO> getTrashNodeByProjectId(@PathVariable String projectId) {
        return apiScenarioModuleService.getTrashNodeTreeByProjectId(projectId);
    }

    @PostMapping("/trash/list/{projectId}")
    public List<ApiScenarioModuleDTO> searchTrashNodeByProjectId(@PathVariable String projectId, @RequestBody ApiScenarioRequest request) {
        return apiScenarioModuleService.getTrashNodeTreeByProjectId(projectId, request);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public String addNode(@RequestBody ApiScenarioModule node) {
        return apiScenarioModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public int editNode(@RequestBody DragApiScenarioModuleRequest node) {
        return apiScenarioModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = ApiScenarioModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return apiScenarioModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = OperLogModule.API_AUTOMATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiScenarioModuleService.class)
    public void dragNode(@RequestBody DragApiScenarioModuleRequest node) {
        apiScenarioModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    @MsRequestLog(module = OperLogModule.API_AUTOMATION)
    public void treeSort(@RequestBody List<String> ids) {
        apiScenarioModuleService.sort(ids);
    }

}
