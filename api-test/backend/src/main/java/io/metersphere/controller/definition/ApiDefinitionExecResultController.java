package io.metersphere.controller.definition;

import io.metersphere.service.definition.ApiDefinitionExecResultService;
import io.metersphere.dto.PlanReportCaseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
}
