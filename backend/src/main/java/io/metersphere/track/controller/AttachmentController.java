package io.metersphere.track.controller;

import io.metersphere.base.domain.FileAttachmentMetadata;
import io.metersphere.service.FileService;
import io.metersphere.track.request.attachment.AttachmentRequest;
import io.metersphere.track.request.testplan.FileOperationRequest;
import io.metersphere.track.service.AttachmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author songcc
 */
@RequestMapping("attachment")
@RestController
public class AttachmentController {

    @Resource
    private FileService fileService;
    @Resource
    private AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public void uploadAttachment(@RequestPart("request") AttachmentRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        attachmentService.uploadAttachment(request, file);
    }

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<byte[]> previewAttachment(@PathVariable String fileId) {
        byte[] bytes = fileService.getAttachmentBytes(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                .body(bytes);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadAttachment(@RequestBody FileOperationRequest fileOperationRequest) {
        byte[] bytes = fileService.getAttachmentBytes(fileOperationRequest.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileOperationRequest.getName(), StandardCharsets.UTF_8) + "\"")
                .contentLength(bytes.length)
                .body(bytes);
    }

    @GetMapping("/delete/{attachmentType}/{attachmentId}")
    public void deleteAttachment(@PathVariable String attachmentId, @PathVariable String attachmentType) {
        attachmentService.deleteAttachment(attachmentId, attachmentType);
    }

    @PostMapping("/metadata/list")
    public List<FileAttachmentMetadata> listMetadata(@RequestBody AttachmentRequest request) {
        return attachmentService.listMetadata(request);
    }
}
