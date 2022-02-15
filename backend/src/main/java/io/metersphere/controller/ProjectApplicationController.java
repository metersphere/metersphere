package io.metersphere.controller;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.ProjectApplicationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/project_application")
public class ProjectApplicationController {
    @Resource
    private ProjectApplicationService projectApplicationService;

    @PostMapping("/update")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#projectApplication)", content = "#msClass.getLogDetails(#projectApplication)", msClass = ProjectApplicationService.class)
    public void updateProject(@RequestBody ProjectApplication projectApplication) {
        projectApplicationService.updateProjectApplication(projectApplication);
    }

    @GetMapping("/get/{projectId}/{type}")
    public ProjectApplication getProjectApplication(@PathVariable String projectId,@PathVariable String type) {
        return projectApplicationService.getProjectApplication(projectId,type);
    }
}
