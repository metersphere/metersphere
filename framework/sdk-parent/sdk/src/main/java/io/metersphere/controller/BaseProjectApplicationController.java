package io.metersphere.controller;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.BaseProjectApplicationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/project_application")
public class BaseProjectApplicationController {
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;

    @GetMapping("/get/{projectId}/{type}")
    public ProjectApplication getProjectApplication(@PathVariable String projectId, @PathVariable String type) {
        return baseProjectApplicationService.getProjectApplication(projectId, type);
    }

    @GetMapping("/get/config/{projectId}")
    public ProjectConfig getProjectConfig(@PathVariable String projectId) {
        return baseProjectApplicationService.getProjectConfig(projectId);
    }

    @GetMapping("/get/config/{projectId}/{type}")
    public ProjectConfig getProjectConfigByType(@PathVariable String projectId, @PathVariable String type) {
        return baseProjectApplicationService.getSpecificTypeValue(projectId, type);
    }
}
