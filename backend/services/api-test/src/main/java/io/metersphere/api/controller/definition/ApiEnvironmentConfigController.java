package io.metersphere.api.controller.definition;

import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.api.service.definition.ApiEnvironmentConfigService;
import io.metersphere.system.utils.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "接口测试-接口管理-环境")
@RestController
@RequestMapping(value = "/api/definition/env")
public class ApiEnvironmentConfigController {
    @Resource
    private ApiEnvironmentConfigService apiEnvironmentConfigService;

    @GetMapping("/get/{projectId}")
    @Operation(summary = "接口测试-接口管理-获取当前用户配置的环境")
    public ApiEnvironmentConfig get(@PathVariable String projectId) {
        return apiEnvironmentConfigService.get(SessionUtils.getUserId(), projectId);
    }

    @GetMapping(value = "/add/{environmentId}")
    @Operation(summary = "接口测试-接口管理-添加当前用户的环境配置")
    public String add(@PathVariable String environmentId) {
        return apiEnvironmentConfigService.add(environmentId, SessionUtils.getUserId(), SessionUtils.getCurrentProjectId());
    }

}
