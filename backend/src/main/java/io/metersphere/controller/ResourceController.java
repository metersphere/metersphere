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
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class ResourceController {

    @Resource
    ResourceService resourceService;
    @PostMapping(value = "/md/upload", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void upload(@RequestPart(value = "request") MdUploadRequest request, @RequestPart(value = "file") MultipartFile file) {
        resourceService.mdUpload(request, file);
    }

    @GetMapping(value = "/md/get/{fileName}")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable("fileName") String fileName) {
        return resourceService.getMdImage(fileName);
    }

    @GetMapping("/md/delete/{fileName}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable("fileName") String fileName) {
        resourceService.mdDelete(fileName);
    }

}
