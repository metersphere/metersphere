package io.metersphere.controller;

import io.metersphere.api.service.JarConfigService;
import io.metersphere.base.domain.JarConfig;
import io.metersphere.commons.constants.RoleConstants;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/api/jar")
@RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER}, logical = Logical.OR)
public class JarConfigController {

    @Resource
    Jarc JarConfigService;

    @GetMapping("/list/{projectId}")
    public List<JarConfig> list(@PathVariable String projectId) {
        return JarConfigService.list(projectId);
    }

    @GetMapping("/get/{id}")
    public JarConfig get(@PathVariable String id) {
        return JarConfigService.get(id);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public String add(@RequestPart("request") JarConfig request, @RequestPart(value = "file") MultipartFile file) {
        return JarConfigService.add(request, file);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestPart("request") JarConfig request, @RequestPart(value = "file", required = false) MultipartFile file) {
        JarConfigService.update(request, file);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        JarConfigService.delete(id);
    }

}
