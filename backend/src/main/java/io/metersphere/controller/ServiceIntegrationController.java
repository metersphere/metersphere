package io.metersphere.controller;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.IntegrationService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("service/integration")
@RestController
public class ServiceIntegrationController {

    @Resource
    private IntegrationService integrationService;

    @PostMapping("/save")
    @MsAuditLog(module = OperLogModule.WORKSPACE_SERVICE_INTEGRATION, type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#service.id)", msClass = IntegrationService.class)
    @RequiresPermissions(PermissionConstants.WORKSPACE_SERVICE_READ_EDIT)
    public ServiceIntegration save(@RequestBody ServiceIntegration service) {
        return integrationService.save(service);
    }

    @PostMapping("/type")
    @RequiresPermissions(PermissionConstants.WORKSPACE_SERVICE_READ)
    public ServiceIntegration getByPlatform(@RequestBody IntegrationRequest request) {
        return integrationService.get(request);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.WORKSPACE_SERVICE_INTEGRATION, type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = IntegrationService.class)
    @RequiresPermissions(PermissionConstants.WORKSPACE_SERVICE_READ_EDIT)
    public void delete(@RequestBody IntegrationRequest request) {
        integrationService.delete(request);
    }

    @GetMapping("/all")
    public List<ServiceIntegration> getAll() {
        return integrationService.getAll(SessionUtils.getCurrentWorkspaceId());
    }

}
