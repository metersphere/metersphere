package io.metersphere.api.controller;

import io.metersphere.api.service.ApiTestJarConfigService;
import io.metersphere.base.domain.ApiTestJarConfig;
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
public class ApiTestJarConfigController {

    @Resource
    ApiTestJarConfigService apiTestJarConfigService;

    @GetMapping("/list/{projectId}")
    public List<ApiTestJarConfig> list(@PathVariable String projectId) {
        return apiTestJarConfigService.list(projectId);
    }

    @GetMapping("/get/{id}")
    public ApiTestJarConfig get(@PathVariable String id) {
        return apiTestJarConfigService.get(id);
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public String add(@RequestPart("request") ApiTestJarConfig request, @RequestPart(value = "file") MultipartFile file) {
        return apiTestJarConfigService.add(request, file);
    }

    @PostMapping(value = "/update", consumes = {"multipart/form-data"})
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void update(@RequestPart("request") ApiTestJarConfig request, @RequestPart(value = "file", required = false) MultipartFile file) {
        apiTestJarConfigService.update(request, file);
    }

    @GetMapping("/delete/{id}")
    @RequiresRoles(value = {RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER,}, logical = Logical.OR)
    public void delete(@PathVariable String id) {
        apiTestJarConfigService.delete(id);
    }

}
