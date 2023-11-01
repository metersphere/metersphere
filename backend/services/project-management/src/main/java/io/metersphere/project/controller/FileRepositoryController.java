package io.metersphere.project.controller;

import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileRepositoryService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "项目管理-文件管理-存储库")
@RestController
@RequestMapping("/project/file/repository")
public class FileRepositoryController {

    @Resource
    private FileRepositoryService fileRepositoryService;
    @Resource
    private FileMetadataService fileMetadataService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "项目管理-文件管理-存储库-存储库列表")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return fileRepositoryService.getTree(projectId);
    }


    @GetMapping(value = "/file-type/{projectId}")
    @Operation(summary = "项目管理-文件管理-获取已存在的存储库文件类型")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    public List<String> getFileType(@PathVariable String projectId) {
        return fileMetadataService.getFileType(projectId, StorageType.GIT.name());
    }
}
