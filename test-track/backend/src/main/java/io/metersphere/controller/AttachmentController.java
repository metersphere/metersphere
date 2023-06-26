package io.metersphere.controller;

import io.metersphere.base.domain.FileAttachmentMetadata;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.constants.AttachmentType;
import io.metersphere.i18n.Translator;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.attachment.AttachmentDumpRequest;
import io.metersphere.xpack.track.dto.AttachmentRequest;
import io.metersphere.service.AttachmentService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author songcc
 */
@RequestMapping("attachment")
@RestController
public class AttachmentController {

    @Resource
    private AttachmentService attachmentService;
    @Resource
    private FileMetadataService fileMetadataService;

    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #file.getOriginalFilename(), false)", msClass = AttachmentService.class)
    @PostMapping(value = "/issue/upload", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    public void uploadIssueAttachment(@RequestPart("request") AttachmentRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        attachmentService.uploadAttachment(request, file);
    }

    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #file.getOriginalFilename(), false)", msClass = AttachmentService.class)
    @PostMapping(value = "/testcase/upload", consumes = {"multipart/form-data"})
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void uploadTestCaseAttachment(@RequestPart("request") AttachmentRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        attachmentService.uploadAttachment(request, file);
    }

    @GetMapping("/preview/{fileId}/{isLocal}")
    public ResponseEntity<byte[]> previewAttachment(@PathVariable String fileId, @PathVariable Boolean isLocal) {
        byte[] bytes;
        if (isLocal) {
            bytes = attachmentService.getAttachmentBytes(fileId);
        } else {
            String refId = attachmentService.getRefIdByAttachmentId(fileId);
            bytes = fileMetadataService.loadFileAsBytes(refId);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                .body(bytes);
    }

    @GetMapping("/download/{id}/{isLocal}")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ, PermissionConstants.PROJECT_TRACK_ISSUE_READ}, logical = Logical.OR)
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("id") String fileId, @PathVariable("isLocal") Boolean isLocal) {
        if (isLocal) {
            return attachmentService.downloadLocalAttachment(fileId);
        } else {
            String refId = attachmentService.getRefIdByAttachmentId(fileId);
            return fileMetadataService.getFile(refId);
        }
    }

    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#attachmentId, 'testcase')", msClass = AttachmentService.class)
    @GetMapping("/delete/testcase/{attachmentId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void deleteTestCaseAttachment(@PathVariable String attachmentId) {
        attachmentService.deleteAttachment(attachmentId, "testcase");
    }

    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#attachmentId, 'issue')", msClass = AttachmentService.class)
    @GetMapping("/delete/issue/{attachmentId}")
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    public void deleteIssueAttachment(@PathVariable String attachmentId) {
        attachmentService.deleteAttachment(attachmentId, "issue");
    }

    @PostMapping("/metadata/list")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ, PermissionConstants.PROJECT_TRACK_ISSUE_READ}, logical = Logical.OR)
    public List<FileAttachmentMetadata> listMetadata(@RequestBody AttachmentRequest request) {
        return attachmentService.listMetadata(request);
    }

    @PostMapping("/testcase/metadata/relate")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds, true)", msClass = AttachmentService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void caseRelate(@RequestBody AttachmentRequest request) {
        if (!AttachmentType.TEST_CASE.type().equals(request.getBelongType())) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        attachmentService.relate(request);
    }

    @PostMapping("/issue/metadata/relate")
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds, true)", msClass = AttachmentService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    public void issueRelate(@RequestBody AttachmentRequest request) {
        if (!AttachmentType.ISSUE.type().equals(request.getBelongType())) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        attachmentService.relate(request);
    }

    @PostMapping("/testcase/metadata/unrelated")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds)", msClass = AttachmentService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT)
    public void caseUnrelated(@RequestBody AttachmentRequest request) {
        if (!AttachmentType.TEST_CASE.type().equals(request.getBelongType())) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        attachmentService.unrelated(request);
    }

    @PostMapping("/issue/metadata/unrelated")
    @MsAuditLog(module = OperLogModule.TRACK_BUG, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds)", msClass = AttachmentService.class)
    @RequiresPermissions(PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT)
    public void issueUnrelated(@RequestBody AttachmentRequest request) {
        if (!AttachmentType.ISSUE.type().equals(request.getBelongType())) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        attachmentService.unrelated(request);
    }

    @PostMapping(value = "/metadata/dump")
    @RequiresPermissions(value = {PermissionConstants.PROJECT_TRACK_CASE_READ_EDIT, PermissionConstants.PROJECT_TRACK_ISSUE_READ_EDIT}, logical = Logical.OR)
    public void dumpFile(@RequestBody AttachmentDumpRequest request) {
        List<MultipartFile> files = new ArrayList<>();
        MultipartFile file = attachmentService.getAttachmentMultipartFile(request.getAttachmentId());
        files.add(file);
        fileMetadataService.dumpFile(request, files);
    }
}
