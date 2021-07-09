package io.metersphere.controller;

import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.controller.request.MdUploadRequest;
import io.metersphere.service.ResourceService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
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

    @GetMapping(value = "/md/get/{fileName}")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable("fileName") String fileName) {
        return resourceService.getMdImage(fileName);
    }

    @GetMapping("/md/delete/{fileName}")
    public void delete(@PathVariable("fileName") String fileName) {
        resourceService.mdDelete(fileName);
    }

}
