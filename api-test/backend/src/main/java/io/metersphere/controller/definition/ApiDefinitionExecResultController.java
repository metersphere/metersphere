package io.metersphere.controller.definition;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.dto.PlanReportCaseDTO;
import io.metersphere.service.definition.ApiDefinitionExecResultService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/definition/exec/result")
public class ApiDefinitionExecResultController {

    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;

    @PostMapping("/plan/report")
    public List<PlanReportCaseDTO> selectForPlanReport(@RequestBody List<String> apiReportIds) {
        return apiDefinitionExecResultService.selectForPlanReport(apiReportIds);
    }

    @PostMapping("/plan/status/map")
    public Map<String, String> selectReportResultByReportIds(@RequestBody List<String> apiReportIds) {
        return apiDefinitionExecResultService.selectReportResultByReportIds(apiReportIds);
    }

    @GetMapping("/last-result/{resourceId}")
    public ApiDefinitionExecResultWithBLOBs selectLastResult(@PathVariable String resourceId) {
        return apiDefinitionExecResultService.getLastResult(resourceId);
    }
}
