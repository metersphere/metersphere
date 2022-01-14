package io.metersphere.controller;

import io.metersphere.controller.request.MdUploadRequest;
import io.metersphere.service.ResourceService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/resource")
public class ResourceController {

    @Resource
    ResourceService resourceService;
    @PostMapping(value = "/md/upload", consumes = {"multipart/form-data"})
    public void upload(@RequestPart(value = "request") MdUploadRequest request, @RequestPart(value = "file", required = false) MultipartFile file) {
        resourceService.mdUpload(request, file);
    }

    @GetMapping(value = "/md/get")
    public ResponseEntity<FileSystemResource> getFile(@RequestParam ("fileName") String fileName) {
        return resourceService.getMdImage(fileName);
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

}
