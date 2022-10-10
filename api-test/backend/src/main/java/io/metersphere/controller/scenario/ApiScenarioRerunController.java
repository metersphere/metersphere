package io.metersphere.controller.scenario;

import io.metersphere.service.scenario.ApiScenarioRerunService;
import io.metersphere.dto.RerunParametersDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/test/exec")
public class ApiScenarioRerunController {

    @Resource
    private ApiScenarioRerunService apiScenarioRerunService;

    @PostMapping("/rerun")
    public String rerun(@RequestBody RerunParametersDTO parametersDTO) {
        return apiScenarioRerunService.rerun(parametersDTO);
    }
}
