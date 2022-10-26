package io.metersphere.controller;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.request.IntegrationRequest;
import io.metersphere.service.BaseIntegrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("service/integration")
@RestController
public class ServiceIntegrationController {

    @Resource
    private BaseIntegrationService baseIntegrationService;

    @PostMapping("/save")
    @MsAuditLog(module = OperLogModule.WORKSPACE_SERVICE_INTEGRATION, type = OperLogConstants.UPDATE, beforeEvent = "#msClass.getLogDetails(#service.workspaceId, #service.platform)",  content = "#msClass.getLogDetails(#service.workspaceId, #service.platform)", msClass = BaseIntegrationService.class)
    public ServiceIntegration save(@RequestBody ServiceIntegration service) {
        return baseIntegrationService.save(service);
    }

    @PostMapping("/type")
    public ServiceIntegration getByPlatform(@RequestBody IntegrationRequest request) {
        return baseIntegrationService.get(request);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = OperLogModule.WORKSPACE_SERVICE_INTEGRATION, title = "#request.platform", type = OperLogConstants.DELETE, msClass = BaseIntegrationService.class)
    public void delete(@RequestBody IntegrationRequest request) {
        baseIntegrationService.delete(request);
    }

    @GetMapping("/all")
    public List<ServiceIntegration> getAll() {
        return baseIntegrationService.getAll(SessionUtils.getCurrentWorkspaceId());
    }

    @GetMapping("/auth/{workspaceId}/{platform}")
    public void authServiceIntegration(@PathVariable String workspaceId, @PathVariable String platform) {
        baseIntegrationService.authServiceIntegration(workspaceId, platform);
    }
}
