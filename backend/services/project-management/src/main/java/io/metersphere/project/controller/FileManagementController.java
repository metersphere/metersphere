package io.metersphere.project.controller;

import io.metersphere.project.dto.FileTableResult;
import io.metersphere.project.request.filemanagement.*;
import io.metersphere.project.service.FileManagementService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "项目管理-文件管理-文件")
@RestController
@RequestMapping("/project/file")
public class FileManagementController {

    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private FileManagementService fileManagementService;

    @PostMapping("/page")
    @Operation(summary = "项目管理-文件管理-表格分页查询文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)
    public FileTableResult page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }


    @PostMapping("/upload")
    @Operation(summary = "项目管理-文件管理-上传文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_ADD)
    public String upload(@Validated @RequestPart("request") FileUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile uploadFile) {
        try {
            return fileMetadataService.upload(request, SessionUtils.getUserId(), uploadFile);
        } catch (Exception e) {
            throw new MSException(Translator.get("upload.file.error"), e);
        }
    }

    @PostMapping("/re-upload")
    @Operation(summary = "项目管理-文件管理-重新上传文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    public String reUpload(@Validated @RequestPart("request") FileReUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile uploadFile) {
        try {
            return fileMetadataService.reUpload(request, SessionUtils.getUserId(), uploadFile);
        } catch (Exception e) {
            throw new MSException(Translator.get("upload.file.error"), e);
        }
    }

    @GetMapping(value = "/download/{id}")
    @Operation(summary = "项目管理-文件管理-下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)
    public ResponseEntity<byte[]> download(@PathVariable String id) throws Exception {
        return fileMetadataService.downloadById(id);
    }

    @PostMapping(value = "/delete")
    @Operation(summary = "项目管理-文件管理-删除文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DELETE)
    public void delete(@Validated @RequestBody FileBatchProcessDTO request) throws Exception {
        fileManagementService.delete(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/update")
    @Operation(summary = "项目管理-文件管理-修改文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_UPDATE)
    public void update(@Validated @RequestBody FileUpdateRequest request) throws Exception {
        fileMetadataService.update(request, SessionUtils.getUserId());
    }

    @PostMapping(value = "/batch-download")
    @Operation(summary = "项目管理-文件管理-批量下载文件")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)
    public ResponseEntity<byte[]> downloadBodyFiles(@Validated @RequestBody FileBatchProcessDTO request) {
        return fileMetadataService.batchDownload(request);
    }
}
