package io.metersphere.controller.scenario;

import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.dto.RerunParametersDTO;
import io.metersphere.dto.TestPlanRerunParametersDTO;
import io.metersphere.log.annotation.MsRequestLog;
import io.metersphere.service.scenario.ApiScenarioRerunService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping(value = "/api/test/exec")
public class ApiScenarioRerunController {

    @Resource
    private ApiScenarioRerunService apiScenarioRerunService;

    @PostMapping("/rerun")
    @MsRequestLog(module = OperLogModule.API_AUTOMATION)
    public String rerun(@RequestBody RerunParametersDTO parametersDTO) {
        return apiScenarioRerunService.rerun(parametersDTO);
    }


    @PostMapping("/rerun/test/plan")
    public String rerunByTestPlan(@RequestBody TestPlanRerunParametersDTO parametersDTO) {
        boolean isStart = apiScenarioRerunService.planRerun(parametersDTO);
        return isStart ? ApiReportStatus.SUCCESS.name() : ApiReportStatus.ERROR.name();
    }
}
