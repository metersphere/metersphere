package io.metersphere.controller;

import io.metersphere.base.domain.ProjectApplication;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.ProjectApplicationRequest;
import io.metersphere.service.ProjectApplicationService;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/project_application")
public class ProjectApplicationController {
    @Resource
    private ProjectApplicationService projectApplicationService;

    @PostMapping("/update")
    @RequiresPermissions("PROJECT_APP_MANAGER:READ+EDIT")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#projectApplication)", content = "#msClass.getLogDetails(#projectApplication)", msClass = ProjectApplicationService.class)
    public void updateProject(@RequestBody ProjectApplication projectApplication) {
        projectApplicationService.updateProjectApplication(projectApplication);
    }

    @PostMapping("/update/batch")
    @RequiresPermissions("PROJECT_APP_MANAGER:READ+EDIT")
    @MsAuditLog(module = OperLogModule.PROJECT_PROJECT_MANAGER, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#projectApplication)", content = "#msClass.getLogDetails(#projectApplication)", msClass = ProjectApplicationService.class)
    public void updateProjectConfigBatch(@RequestBody ProjectApplicationRequest request) {
        projectApplicationService.updateProjectConfigBatch(request);
    }

}
