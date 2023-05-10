package io.metersphere.controller.definition;

import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.definition.ApiDefinitionEnvService;
import io.metersphere.base.domain.ApiDefinitionEnv;
import io.metersphere.commons.constants.PermissionConstants;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

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
    @RequiresPermissions(value = {PermissionConstants.PROJECT_API_DEFINITION_READ_CREATE_API, PermissionConstants.PROJECT_API_DEFINITION_READ}, logical = Logical.OR)
    @MsRequestLog(module = OperLogModule.API_DEFINITION)
    public void create(@RequestBody ApiDefinitionEnv request) {
        apiDefinitionEnvService.insert(request);
    }
}
