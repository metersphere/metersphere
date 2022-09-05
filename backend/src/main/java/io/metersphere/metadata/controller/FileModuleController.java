package io.metersphere.metadata.controller;

import io.metersphere.base.domain.FileModule;
import io.metersphere.commons.constants.FileModuleTypeConstants;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.service.FileModuleService;
import io.metersphere.metadata.utils.GitRepositoryUtils;
import io.metersphere.metadata.vo.DragFileModuleRequest;
import io.metersphere.metadata.vo.FileModuleVo;
import io.metersphere.service.CheckPermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/file/module")
@RestController
public class FileModuleController {

    @Resource
    FileModuleService fileModuleService;
    @Resource
    private CheckPermissionService checkPermissionService;

    @GetMapping("/list/{projectId}")
    public List<FileModuleVo> getNodeByProjectId(@PathVariable String projectId) {
        checkPermissionService.checkProjectOwner(projectId);
        return fileModuleService.getNodeTreeByProjectId(projectId);
    }

    @PostMapping("/add")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.CREATE, title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public String addNode(@RequestBody FileModule node) {
        return fileModuleService.addNode(node);
    }

    @PostMapping("/connect")
    public String connect(@RequestBody FileModule node) {
        if (StringUtils.equalsIgnoreCase(node.getModuleType(),FileModuleTypeConstants.REPOSITORY.getValue())){
            GitRepositoryUtils utils = new GitRepositoryUtils(node.getRepositoryPath(),node.getRepositoryUserName(),node.getRepositoryToken());
            utils.getBranches();
        }
        return "suucess";
    }

    @PostMapping("/edit")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public int editNode(@RequestBody DragFileModuleRequest node) {
        return fileModuleService.editNode(node);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#nodeIds)", msClass = FileModuleService.class)
    public int deleteNode(@RequestBody List<String> nodeIds) {
        return fileModuleService.deleteNode(nodeIds);
    }

    @PostMapping("/drag")
    @MsAuditLog(module = OperLogModule.PROJECT_FILE_MANAGEMENT, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#node)", title = "#node.name", content = "#msClass.getLogDetails(#node)", msClass = FileModuleService.class)
    public void dragNode(@RequestBody DragFileModuleRequest node) {
        fileModuleService.dragNode(node);
    }

    @PostMapping("/pos")
    public void treeSort(@RequestBody List<String> ids) {
        fileModuleService.sort(ids);
    }

}
