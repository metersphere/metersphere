package io.metersphere.controller.plan;

import io.metersphere.api.dto.ApiReportResult;
import io.metersphere.service.ShareInfoService;
import io.metersphere.service.definition.ApiDefinitionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/share")
public class ShareTestPlanApiReportController {

    @Resource
    ApiDefinitionService apiDefinitionService;
    @Resource
    ShareInfoService shareInfoService;

    @GetMapping("/api/definition/report/getReport/{shareId}/{testId}")
    public ApiReportResult getApiReport(@PathVariable String shareId, @PathVariable String testId) {
        shareInfoService.validate(shareId);
        return apiDefinitionService.getDbResult(testId);
    }

    @GetMapping("/api/definition/report/by/id/{reportId}")
    public ApiReportResult getApiReport(@PathVariable String reportId) {
        return apiDefinitionService.getReportById(reportId);
    }

}
