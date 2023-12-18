package io.metersphere.project.controller;

import io.metersphere.project.dto.filemanagement.request.FileModuleCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileModuleUpdateRequest;
import io.metersphere.project.service.FileModuleService;
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

@Tag(name = "项目管理-文件管理-模块")
@RestController
@RequestMapping("/project/file-module")
public class FileModuleController {

    @Resource
    private FileModuleService fileModuleService;

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "项目管理-文件管理-模块-查找模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-文件管理-模块-添加模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@RequestBody @Validated FileModuleCreateRequest request) {
        return fileModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-文件管理-模块-修改模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "file_module")
    public boolean list(@RequestBody @Validated FileModuleUpdateRequest request) {
        fileModuleService.update(request, SessionUtils.getUserId());
        return true;
    }

    @GetMapping("/delete/{deleteId}")
    @Operation(summary = "项目管理-文件管理-模块-删除模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE)
    @CheckOwner(resourceId = "#deleteId", resourceType = "file_module")
    public void deleteNode(@PathVariable String deleteId) {
        fileModuleService.deleteModule(deleteId, SessionUtils.getUserId());
    }

    @PostMapping("/move")
    @Operation(summary = "项目管理-文件管理-模块-移动模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getDragNodeId()", resourceType = "file_module")
    public void moveNode(@Validated @RequestBody NodeMoveRequest request) {
        fileModuleService.moveNode(request, SessionUtils.getUserId());
    }
}
