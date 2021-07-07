package io.metersphere.api.controller;

import io.metersphere.api.dto.definition.ApiModuleDTO;
import io.metersphere.api.dto.definition.DragModuleRequest;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.ApiDefinitionDefaultApiTypeUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.CheckPermissionService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/api/module")
@RestController
public class ApiModuleController {

    @Resource
    ApiModuleService apiModuleService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}/{protocol}")
    public List<ApiModuleDTO> getNodeByProjectId(@PathVariable String projectId, @PathVariable String protocol) {
//        checkPermissionService.checkProjectOwner(projectId);
        String userId = SessionUtils.getUserId();
        ApiDefinitionDefaultApiTypeUtil.addUserSelectApiType(userId, protocol);
        return apiModuleService.getNodeTreeByProjectId(projectId, protocol);
    }

    @GetMapping("/getModuleByName/{projectId}/{protocol}")
    public ApiModule getModuleByName(@PathVariable String projectId, @PathVariable String protocol) {
//        checkPermissionService.checkProjectOwner(projectId);
        return apiModuleService.getModuleByName(projectId, protocol);
    }

    @GetMapping("/getUserDefaultApiType")
    public String getUserDefaultApiType() {
        String returnStr = ApiDefinitionDefaultApiTypeUtil.HTTP;
        try {
            String userId = SessionUtils.getUserId();
            returnStr = ApiDefinitionDefaultApiTypeUtil.getUserSelectedApiType(userId);
        } catch (Exception e) {

        }
        return returnStr;
    }

    @GetMapping("/list/plan/{planId}/{protocol}")
    public List<ApiModuleDTO> getNodeByPlanId(@PathVariable String planId, @PathVariable String protocol) {
        checkPermissionService.checkTestPlanOwner(planId);
        return apiModuleService.getNodeByPlanId(planId, protocol);
    }

    @PostMapping("/add")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public String addNode(@RequestBody ApiModule node) {
        return apiModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public int editNode(@RequestBody DragModuleRequest node) {
        return apiModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = ApiModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return apiModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = "api_definition", type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public void dragNode(@RequestBody DragModuleRequest node) {
        apiModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    public void treeSort(@RequestBody List<String> ids) {
        apiModuleService.sort(ids);
    }

}
