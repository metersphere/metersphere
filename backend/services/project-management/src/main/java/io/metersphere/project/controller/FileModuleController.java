package io.metersphere.project.controller;

import io.metersphere.project.request.filemanagement.FileModuleCreateRequest;
import io.metersphere.project.request.filemanagement.FileModuleUpdateRequest;
import io.metersphere.project.service.FileModuleLogService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
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
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/add")
    @Operation(summary = "项目管理-文件管理-模块-添加模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    public String add(@RequestBody @Validated FileModuleCreateRequest request) {
        return fileModuleService.add(request, SessionUtils.getUserId());
    }

    @PostMapping("/update")
    @Operation(summary = "项目管理-文件管理-模块-修改模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    public boolean list(@RequestBody @Validated FileModuleUpdateRequest request) {
        fileModuleService.update(request, SessionUtils.getUserId());
        return true;
    }

    @GetMapping("/delete/{deleteId}")
    @Operation(summary = "项目管理-文件管理-模块-删除模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE)
    @Log(type = OperationLogType.DELETE, expression = "#msClass.deleteLog(#deleteId)", msClass = FileModuleLogService.class)
    public void deleteNode(@PathVariable String deleteId) {
        fileModuleService.deleteModule(deleteId);
    }

    @PostMapping("/move")
    @Operation(summary = "项目管理-文件管理-模块-移动模块")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    public void moveNode(@RequestBody NodeMoveRequest request) {
        /**
         * 拖拽操作。  两种：同级移动 和 跨级移动
         *  1.判断移动后的parentID，判断是否是移动到其余的目录下
         *  2.拖拽后的前后ID。 用于排序。
         */
        fileModuleService.moveNode(request);
    }
}
