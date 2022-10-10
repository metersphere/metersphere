package io.metersphere.controller.definition;

import io.metersphere.api.dto.definition.ApiModuleDTO;
import io.metersphere.api.dto.definition.DragModuleRequest;
import io.metersphere.service.definition.ApiModuleService;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.ApiDefinitionDefaultApiTypeUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/api/module")
@RestController
public class ApiModuleController {

    @Resource
    ApiModuleService apiModuleService;

    @GetMapping("/list/{projectId}/{protocol}")
    public List<ApiModuleDTO> getNodeByProjectId(@PathVariable String projectId, @PathVariable String protocol) {
        String userId = SessionUtils.getUserId();
        ApiDefinitionDefaultApiTypeUtil.addUserSelectApiType(userId, protocol);
        return apiModuleService.getNodeTreeByProjectId(projectId, protocol, null);
    }

    @GetMapping("/list/{projectId}/{protocol}/{versionId}")
    public List<ApiModuleDTO> getNodeByProjectId(@PathVariable String projectId, @PathVariable String protocol,
                                                 @PathVariable String versionId) {
        String userId = SessionUtils.getUserId();
        ApiDefinitionDefaultApiTypeUtil.addUserSelectApiType(userId, protocol);
        return apiModuleService.getNodeTreeByProjectId(projectId, protocol, versionId);
    }

    @GetMapping("/trash/list/{projectId}/{protocol}/{versionId}")
    public List<ApiModuleDTO> getTrashNodeByProtocolAndProjectId(@PathVariable String projectId, @PathVariable String protocol,
                                                                 @PathVariable String versionId) {
        return apiModuleService.getTrashNodeTreeByProtocolAndProjectId(projectId, protocol, versionId);
    }

    @GetMapping("/trash/list/{projectId}/{protocol}")
    public List<ApiModuleDTO> getTrashNodeByProtocolAndProjectId(@PathVariable String projectId, @PathVariable String protocol) {
        return apiModuleService.getTrashNodeTreeByProtocolAndProjectId(projectId, protocol, null);
    }

    @GetMapping("/trash-count/{projectId}/{protocol}")
    public long trashCount(@PathVariable String projectId, @PathVariable String protocol) {
        String userId = SessionUtils.getUserId();
        ApiDefinitionDefaultApiTypeUtil.addUserSelectApiType(userId, protocol);
        return apiModuleService.countTrashApiData(projectId, protocol);
    }

    @GetMapping("/default-type")
    public String getUserDefaultApiType() {
        String returnStr = ApiDefinitionDefaultApiTypeUtil.HTTP;
        try {
            String userId = SessionUtils.getUserId();
            returnStr = ApiDefinitionDefaultApiTypeUtil.getUserSelectedApiType(userId);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return returnStr;
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public String addNode(@RequestBody ApiModule node) {
        return apiModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public int editNode(@RequestBody DragModuleRequest node) {
        return apiModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = ApiModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        //nodeIds 包含删除节点ID及其所有子节点ID
        return apiModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = OperLogModule.API_DEFINITION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = ApiModuleService.class)
    public void dragNode(@RequestBody DragModuleRequest node) {
        apiModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    public void treeSort(@RequestBody List<String> ids) {
        apiModuleService.sort(ids);
    }

}
