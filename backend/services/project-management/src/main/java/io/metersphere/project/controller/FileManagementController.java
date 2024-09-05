package io.metersphere.project.controller;

import io.metersphere.project.dto.filemanagement.request.*;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.dto.filemanagement.response.FileVersionResponse;
import io.metersphere.project.service.FileManagementService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.system.file.annotation.FileLimit;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "项目管理-文件管理-文件")
@RestController
@RequestMapping("/project/file")
public class FileManagementController {

    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileManagementService fileManagementService;

    @GetMapping(value = "/type/{projectId}")
    @Operation(summary = "项目管理-文件管理-获取已存在的文件类型")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<String> getFileType(@PathVariable String projectId) {
        return fileMetadataService.getFileType(projectId, StorageType.MINIO.name());
    }

    @PostMapping("/page")
    @Operation(summary = "项目管理-文件管理-表格分页查询文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FileInformationResponse>> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }

    @GetMapping("/get/{id}")
    @Operation(summary = "项目管理-文件管理-查看文件详情")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#id", resourceType = "file_metadata")
    public FileInformationResponse page(@PathVariable String id) {
        return fileMetadataService.getFileInformation(id);
    }


    @PostMapping("/module/count")
    @Operation(summary = "项目管理-文件管理-表格分页查询文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Map<String, Long> moduleCount(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.moduleCount(request, SessionUtils.getUserId());
    }

    @FileLimit
    @PostMapping("/upload")
    @Operation(summary = "项目管理-文件管理-上传文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String upload(@Validated @RequestPart("request") FileUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile uploadFile) throws Exception {
        return fileMetadataService.upload(request, SessionUtils.getUserId(), uploadFile);
    }

    @FileLimit
    @PostMapping("/re-upload")
    @Operation(summary = "项目管理-文件管理-重新上传文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getFileId()", resourceType = "file_metadata")
    public String reUpload(@Validated @RequestPart("request") FileReUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile uploadFile) throws Exception {
        return fileMetadataService.reUpload(request, SessionUtils.getUserId(), uploadFile);
    }

    @GetMapping(value = "/download/{id}")
    @Operation(summary = "项目管理-文件管理-下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)
    @CheckOwner(resourceId = "#id", resourceType = "file_metadata")
    public ResponseEntity<byte[]> download(@PathVariable String id) throws Exception {
        return fileMetadataService.downloadById(id);
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "项目管理-文件管理-删除文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "file_metadata")
    public void delete(@Validated @RequestBody FileBatchProcessRequest request) throws Exception {
        fileManagementService.delete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "项目管理-文件管理-修改文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getId()", resourceType = "file_metadata")
    public void update(@Validated @RequestBody FileUpdateRequest request) throws Exception {
        fileMetadataService.update(request, SessionUtils.getUserId());
    }

    @GetMapping(value = "/jar-file-status/{fileId}/{enable}")
    @Operation(summary = "项目管理-文件管理-Jar文件启用禁用操作")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#fileId", resourceType = "file_metadata")
    public void changeJarFileStatus(@PathVariable String fileId, @PathVariable boolean enable) {
        fileMetadataService.changeJarFileStatus(fileId, enable, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-download")
    @Operation(summary = "项目管理-文件管理-批量下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "file_metadata")
    public void downloadBodyFiles(@Validated @RequestBody FileBatchProcessRequest request, HttpServletResponse httpServletResponse) {
        fileMetadataService.batchDownload(request, httpServletResponse);
    }

    @PostMapping(value = "/batch-move")
    @Operation(summary = "项目管理-文件管理-批量移动文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    @CheckOwner(resourceId = "#request.getSelectIds()", resourceType = "file_metadata")
    public void batchMoveFiles(@Validated @RequestBody FileBatchMoveRequest request) {
        fileMetadataService.batchMove(request, SessionUtils.getUserId());
    }


    @GetMapping(value = "/file-version/{fileId}")
    @Operation(summary = "项目管理-文件管理-文件历史版本")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    @CheckOwner(resourceId = "#fileId", resourceType = "file_metadata")
    public List<FileVersionResponse> getFileVersion(@PathVariable String fileId) {
        return fileMetadataService.getFileVersion(fileId);
    }
}
