package io.metersphere.controller;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.OperLogConstants;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.log.annotation.MsAuditLog;
import io.metersphere.service.IntegrationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("service/integration")
@RestController
public class ServiceIntegrationController {

    @Resource
    private IntegrationService integrationService;

    @PostMapping("/save")
    @MsAuditLog(module = "organization_service_integration", type = OperLogConstants.CREATE, content = "#msClass.getLogDetails(#service.id)", msClass = IntegrationService.class)
    public ServiceIntegration save(@RequestBody ServiceIntegration service) {
        return integrationService.save(service);
    }

    @PostMapping("/type")
    public ServiceIntegration getByPlatform(@RequestBody IntegrationRequest request) {
        return integrationService.get(request);
    }

    @PostMapping("/delete")
    @MsAuditLog(module = "organization_service_integration", type = OperLogConstants.DELETE, beforeEvent = "#msClass.getLogDetails(#request.id)", msClass = IntegrationService.class)
    public void delete(@RequestBody IntegrationRequest request) {
        integrationService.delete(request);
    }

    @GetMapping("/all/{orgId}")
    public List<ServiceIntegration> getAll(@PathVariable String orgId) {
        return integrationService.getAll(orgId);
    }

}
