package io.metersphere.project.controller;

import io.metersphere.project.dto.filemanagement.FileInformationDTO;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.service.PermissionCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "项目管理-文件预览")
@RestController
@RequestMapping("/file/preview")
public class FilePreviewController {

    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private PermissionCheckService permissionCheckService;


    @GetMapping(value = "/original/{userId}/{fileId}")
    @Operation(summary = "预览原图")
    @RequiresPermissions(PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)
    public ResponseEntity<byte[]> originalImg(@PathVariable String userId,@PathVariable String fileId) {
        FileInformationDTO fileInformationDTO = fileMetadataService.get(fileId);
        if (fileInformationDTO == null) {
            throw new MSException("file.not.exist");
        }
        //检查权限
        if(permissionCheckService.userHasProjectPermission(userId,fileInformationDTO.getProjectId(),PermissionConstants.PROJECT_FILE_MANAGEMENT_READ_DOWNLOAD)){
            return fileMetadataService.downloadById(fileId);
        }else {
            throw  new MSException("http_result_forbidden");
        }

    }
    @GetMapping(value = "/compressed/{userId}/{fileId}")
    @Operation(summary = "预览缩略图")
    public ResponseEntity<byte[]> compressedImg(@PathVariable String userId,@PathVariable String fileId) {
        FileInformationDTO fileInformationDTO = fileMetadataService.get(fileId);
        if (fileInformationDTO == null) {
            throw new MSException("file.not.exist");
        }
        //检查权限
        if(permissionCheckService.userHasProjectPermission(userId,fileInformationDTO.getProjectId(),PermissionConstants.PROJECT_FILE_MANAGEMENT_READ)){
            return fileMetadataService.downloadPreviewImgById(fileId);
        }else {
            throw  new MSException("http_result_forbidden");
        }

    }
}
