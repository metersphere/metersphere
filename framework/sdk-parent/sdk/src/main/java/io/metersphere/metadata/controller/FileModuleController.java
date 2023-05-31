package io.metersphere.metadata.controller;

import io.metersphere.base.domain.FileModule;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.service.FileModuleService;
import io.metersphere.metadata.vo.DragFileModuleRequest;
import io.metersphere.metadata.vo.FileModuleVo;
import io.metersphere.service.BaseCheckPermissionService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/file/module")
@RestController
public class FileModuleController {

    @Resource
    FileModuleService fileModuleService;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;

    @GetMapping("/list/{projectId}")
    public List<FileModuleVo> getNodeByProjectId(@PathVariable String projectId) {
        baseCheckPermissionService.checkProjectOwner(projectId);
        return fileModuleService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/add")
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public String addNode(@RequestBody FileModule node) {
        return fileModuleService.addNode(node);
    }

    @PostMapping("/edit")
    @RequiresPermissions("PROJECT_FILE:READ+UPLOAD+JAR")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public int editNode(@RequestBody DragFileModuleRequest node) {
        return fileModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+BATCH+DELETE", "PROJECT_FILE:READ+DELETE+JAR"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = FileModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return fileModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+UPLOAD+JAR", "PROJECT_FILE:READ+BATCH+MOVE"}, logical = Logical.OR)
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public void dragNode(@RequestBody DragFileModuleRequest node) {
        fileModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    @RequiresPermissions(value = {"PROJECT_FILE:READ+UPLOAD+JAR", "PROJECT_FILE:READ+BATCH+MOVE"}, logical = Logical.OR)
    public void treeSort(@RequestBody List<String> ids) {
        fileModuleService.sort(ids);
    }

}
