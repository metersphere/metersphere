package io.metersphere.bug.controller;

import io.metersphere.bug.dto.request.BugDeleteFileRequest;
import io.metersphere.bug.dto.request.BugFileSourceRequest;
import io.metersphere.bug.dto.request.BugFileTransferRequest;
import io.metersphere.bug.dto.request.BugUploadFileRequest;
import io.metersphere.bug.dto.response.BugFileDTO;
import io.metersphere.bug.service.BugAttachmentLogService;
import io.metersphere.bug.service.BugAttachmentService;
import io.metersphere.project.dto.filemanagement.request.FileMetadataTableRequest;
import io.metersphere.project.dto.filemanagement.response.FileInformationResponse;
import io.metersphere.project.service.FileAssociationService;
import io.metersphere.project.service.FileMetadataService;
import io.metersphere.project.service.FileModuleService;
import io.metersphere.sdk.constants.PermissionConstants;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.annotation.Log;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.security.CheckOwner;
import io.metersphere.system.service.CommonFileService;
import io.metersphere.system.utils.Pager;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "缺陷管理-附件")
@RestController
@RequestMapping("/bug/attachment")
public class BugAttachmentController {

    @Resource
    private FileModuleService fileModuleService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private BugAttachmentService bugAttachmentService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private CommonFileService commonFileService;

    @GetMapping("/list/{bugId}")
    @Operation(summary = "缺陷管理-附件-列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#bugId", resourceType = "bug")
    public List<BugFileDTO> page(@PathVariable String bugId) {
        return bugAttachmentService.getAllBugFiles(bugId);
    }

    @PostMapping("/file/page")
    @Operation(summary = "缺陷管理-附件-关联文件库文件列表")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public Pager<List<FileInformationResponse>> page(@Validated @RequestBody FileMetadataTableRequest request) {
        return fileMetadataService.page(request);
    }

    @PostMapping("/upload")
    @Operation(summary = "缺陷管理-附件-上传/关联文件")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.uploadLog(#request, #file)", msClass = BugAttachmentLogService.class)
    public void uploadFile(@Validated @RequestPart("request") BugUploadFileRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        bugAttachmentService.uploadFile(request, file, SessionUtils.getUserId());
    }

    @PostMapping("/delete")
    @Operation(summary = "缺陷管理-附件-删除/取消关联文件")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_UPDATE)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    @Log(type = OperationLogType.UPDATE, expression = "#msClass.deleteLog(#request)", msClass = BugAttachmentLogService.class)
    public void deleteFile(@RequestBody BugDeleteFileRequest request) {
        bugAttachmentService.deleteFile(request);
    }

    @PostMapping("/preview")
    @Operation(summary = "缺陷管理-附件-预览")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ResponseEntity<byte[]> preview(@Validated @RequestBody BugFileSourceRequest request) {
        if (request.getAssociated()) {
            // 文件库
            return fileMetadataService.downloadPreviewImgById(request.getFileId());
        } else {
            // 本地
            return bugAttachmentService.downloadOrPreview(request);
        }
    }

    @PostMapping("/download")
    @Operation(summary = "缺陷管理-附件-下载")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public ResponseEntity<byte[]> download(@Validated @RequestBody BugFileSourceRequest request) {
        if (request.getAssociated()) {
            // 文件库
            return fileMetadataService.downloadById(request.getFileId());
        } else {
            // 本地
            return bugAttachmentService.downloadOrPreview(request);
        }
    }

    @GetMapping("/transfer/options/{projectId}")
    @Operation(summary = "缺陷管理-附件-转存文件库模块集合")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#projectId", resourceType = "project")
    public List<BaseTreeNode> options(@PathVariable String projectId) {
        return fileModuleService.getTree(projectId);
    }

    @PostMapping("/transfer")
    @Operation(summary = "缺陷管理-附件-本地文件转存")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String transfer(@Validated @RequestBody BugFileTransferRequest request) {
        return bugAttachmentService.transfer(request, SessionUtils.getUserId());
    }

    @PostMapping("/check-update")
    @Operation(summary = "缺陷管理-附件-检查关联文件集合是否存在更新")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    public List<String> checkUpdate(@RequestBody List<String> fileIds) {
        return fileAssociationService.checkFilesVersion(fileIds);
    }

    @PostMapping("/update")
    @Operation(summary = "缺陷管理-附件-更新关联文件")
    @RequiresPermissions(PermissionConstants.PROJECT_BUG_READ)
    @CheckOwner(resourceId = "#request.getProjectId()", resourceType = "project")
    public String update(@Validated @RequestBody BugDeleteFileRequest request) {
        return bugAttachmentService.upgrade(request, SessionUtils.getUserId());
    }

    @PostMapping("/upload/md/file")
    @Operation(summary = "缺陷管理-富文本附件-上传")
    @RequiresPermissions(logical = Logical.OR, value = {PermissionConstants.PROJECT_BUG_ADD, PermissionConstants.PROJECT_BUG_UPDATE, PermissionConstants.PROJECT_BUG_COMMENT})
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        return commonFileService.uploadTempImgFile(file);
    }

    @GetMapping(value = "/preview/md/{projectId}/{fileId}/{compressed}")
    @Operation(summary = "缺陷管理-富文本缩略图-预览")
    public ResponseEntity<byte[]> previewMd(@PathVariable String projectId, @PathVariable String fileId, @PathVariable("compressed") boolean compressed) {
        return bugAttachmentService.previewMd(projectId, fileId, compressed);
    }
}
