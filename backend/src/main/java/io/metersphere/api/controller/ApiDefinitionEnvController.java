package io.metersphere.api.controller;

import io.metersphere.api.service.ApiDefinitionEnvService;
import io.metersphere.base.domain.ApiDefinitionEnv;
import io.metersphere.commons.constants.PermissionConstants;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/definition/env")
public class ApiDefinitionEnvController {
    @Resource
    private ApiDefinitionEnvService apiDefinitionEnvService;

    @GetMapping("/get/{id}/{projectId}")
    public ApiDefinitionEnv get(@PathVariable String id, @PathVariable String projectId) {
        return apiDefinitionEnvService.get(id, projectId);
    }

    @PostMapping(value = "/create")
    @RequiresPermissions(PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_API)
    public void create(@RequestBody ApiDefinitionEnv request) {
        apiDefinitionEnvService.insert(request);
    }
}
