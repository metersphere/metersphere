package io.metersphere.api.controller;

import io.metersphere.api.dto.ScenarioToPerformanceInfoDTO;
import io.metersphere.api.service.ApiPerformanceService;
import io.metersphere.commons.constants.PermissionConstants;
import io.metersphere.request.EditTestPlanRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
public class ApiPerformanceController {
    @Resource
    private ApiPerformanceService apiPerformanceService;

    @PostMapping(value = "api/automation/export/jmx")
    public ScenarioToPerformanceInfoDTO exportJmx(@RequestBody Object request) {
        return apiPerformanceService.exportJmx(request);
    }

    @PostMapping("api/automation/list/{goPage}/{pageSize}")
    public Object list(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody Object request) {
        return apiPerformanceService.list(goPage, pageSize, request);
    }

    @PostMapping(value = "/performance/sync/scenario")
    @RequiresPermissions(PermissionConstants.PROJECT_PERFORMANCE_TEST_READ_CREATE)
    public void syncScenario(@RequestBody EditTestPlanRequest request) {
        apiPerformanceService.syncApi(request);
    }
}
