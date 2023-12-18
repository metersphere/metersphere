package io.metersphere.project.controller;

import io.metersphere.project.dto.filemanagement.request.FileRepositoryConnectRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryCreateRequest;
import io.metersphere.project.dto.filemanagement.request.FileRepositoryUpdateRequest;
import io.metersphere.project.dto.filemanagement.request.RepositoryFileAddRequest;
import io.metersphere.project.dto.filemanagement.response.FileRepositoryResponse;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileRepositoryService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> getTree(@PathVariable String projectId) {
        return fileRepositoryService.getTree(projectId);
    }

    @GetMapping(value = "/file-type/{projectId}")
    @Operation(summary = "项目管理-文件管理-存储库-获取已存在的存储库文件类型")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<String> getFileType(@PathVariable String projectId) {
        return fileMetadataService.getFileType(projectId, StorageType.GIT.name());
    }

    @PostMapping("/add-repository")
    @Operation(summary = "项目管理-文件管理-存储库-添加存储库")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String add(@RequestBody @Validated FileRepositoryCreateRequest request) {
        return fileRepositoryService.addRepository(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/info/{id}")
    @Operation(summary = "项目管理-文件管理-存储库-存储库信息")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#id", resourceType = "file_module")
    public FileRepositoryResponse getRepositoryInfo(@PathVariable String id) {
        return fileRepositoryService.getRepositoryInfo(id);
    }

    @PostMapping("/update-repository")
    @Operation(summary = "项目管理-文件管理-存储库-修改存储库")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "file_module")
    public boolean list(@RequestBody @Validated FileRepositoryUpdateRequest request) {
        fileRepositoryService.updateRepository(request, SessionUtils.getUserId());
        return true;
    }

    @PostMapping("/connect")
    @Operation(summary = "项目管理-文件管理-存储库-测试存储库链接")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    public void connect(@RequestBody @Validated FileRepositoryConnectRequest request) {
        fileRepositoryService.connect(request.getUrl(), request.getToken(), request.getUserName());
    }

    @PostMapping("/add-file")
    @Operation(summary = "项目管理-文件管理-存储库-添加文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getModuleId()", resourceType = "file_module")
    public String addFile(@Validated @RequestBody RepositoryFileAddRequest request) throws Exception {
        return fileRepositoryService.addFile(request, SessionUtils.getUserId());
    }

    @GetMapping("/pull-file/{id}")
    @Operation(summary = "项目管理-文件管理-存储库-更新文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#id", resourceType = "file_metadata")
    public String pullFile(@PathVariable String id) throws Exception {
        return fileMetadataService.pullFile(id, SessionUtils.getUserId());
    }
}
