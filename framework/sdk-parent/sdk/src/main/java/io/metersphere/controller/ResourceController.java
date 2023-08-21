package io.metersphere.controller;

import io.metersphere.request.MdImageSaveRequest;
import io.metersphere.request.MdUploadRequest;
import io.metersphere.service.ResourceService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    @Resource
    ResourceService resourceService;
    @PostMapping(value = "/md/upload", consumes = {"multipart/form-data"})
    public String upload(@RequestPart(value = "request") MdUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return resourceService.mdUpload(request, file);
    }

    @PostMapping(value = "/md/temp/upload", consumes = {"multipart/form-data"})
    public String upload2Temp(@RequestPart(value = "request") MdUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        return resourceService.mdUpload2Temp(request, file);
    }

    @GetMapping(value = "/md/get")
    public ResponseEntity<FileSystemResource> getFile(@RequestParam ("fileName") String fileName) {
        return resourceService.getMdImage(fileName);
    }

    @GetMapping(value = "/ui/get")
    public ResponseEntity<FileSystemResource> getUiFile(@RequestParam ("fileName") String fileName, @RequestParam ("reportId") String reportId) {
        return resourceService.getUiResultImage(fileName, reportId);
    }

    /**
     * 兼容旧版本
     * @param fileName
     * @return
     */
    @GetMapping(value = "/md/get/{fileName}")
    public ResponseEntity<FileSystemResource> getFileCompatible(@PathVariable("fileName") String fileName) {
        return resourceService.getMdImage(fileName);
    }

    @GetMapping("/md/delete/{fileName}")
    public void delete(@PathVariable("fileName") String fileName) {
        resourceService.mdDelete(fileName);
    }

    @PostMapping("/md/save/image")
    public void saveMdImages(@RequestBody MdImageSaveRequest request) {
        resourceService.saveMdImages(request);
    }
}
