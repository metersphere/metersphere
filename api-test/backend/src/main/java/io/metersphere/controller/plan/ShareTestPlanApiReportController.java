package io.metersphere.controller.plan;

import io.metersphere.api.dto.ApiReportResult;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.definition.ApiDefinitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/share")
public class ShareTestPlanApiReportController {

    @Resource
    ApiDefinitionService apiDefinitionService;
    @Resource
    ShareInfoService shareInfoService;

    @GetMapping("/api/definition/report/getReport/{shareId}/{testId}")
    public ApiReportResult getApiReport(@PathVariable String shareId, @PathVariable String testId) {
        shareInfoService.validateExpired(shareId);
        return apiDefinitionService.getDbResult(testId);
    }
}
