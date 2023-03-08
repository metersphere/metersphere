package io.metersphere.controller;

import io.metersphere.base.domain.FileAttachmentMetadata;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.request.attachment.AttachmentDumpRequest;
import io.metersphere.xpack.track.dto.AttachmentRequest;
import io.metersphere.service.AttachmentService;
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

    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, title = "#request.belongType", content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #file.getOriginalFilename(), false)", msClass = AttachmentService.class)
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public void uploadAttachment(@RequestPart("request") AttachmentRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
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
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("id") String fileId, @PathVariable("isLocal") Boolean isLocal) {
        if (isLocal) {
            return attachmentService.downloadLocalAttachment(fileId);
        } else {
            String refId = attachmentService.getRefIdByAttachmentId(fileId);
            return fileMetadataService.getFile(refId);
        }
    }

    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#attachmentId, #attachmentType)", title = "#request.belongType", msClass = AttachmentService.class)
    @GetMapping("/delete/{attachmentType}/{attachmentId}")
    public void deleteAttachment(@PathVariable String attachmentId, @PathVariable String attachmentType) {
        attachmentService.deleteAttachment(attachmentId, attachmentType);
    }

    @PostMapping("/metadata/list")
    public List<FileAttachmentMetadata> listMetadata(@RequestBody AttachmentRequest request) {
        return attachmentService.listMetadata(request);
    }

    @PostMapping("/metadata/relate")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, title = "#request.belongType", content = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds, true)", msClass = AttachmentService.class)
    public void relate(@RequestBody AttachmentRequest request) {
        attachmentService.relate(request);
    }

    @PostMapping("/metadata/unrelated")
    @MsAuditLog(module = OperLogModule.TRACK_TEST_CASE, type = OperLogConstants.UPDATE, title = "#request.belongType", beforeEvent = "#msClass.getLogDetails(#request.belongId, #request.belongType, #request.metadataRefIds)", msClass = AttachmentService.class)
    public void unrelated(@RequestBody AttachmentRequest request) {
        attachmentService.unrelated(request);
    }

    @PostMapping(value = "/metadata/dump")
    public void dumpFile(@RequestBody AttachmentDumpRequest request) {
        List<MultipartFile> files = new ArrayList<>();
        MultipartFile file = attachmentService.getAttachmentMultipartFile(request.getAttachmentId());
        files.add(file);
        fileMetadataService.dumpFile(request, files);
    }
}
