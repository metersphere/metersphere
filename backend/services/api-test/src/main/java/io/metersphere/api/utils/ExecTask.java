package io.metersphere.api.utils;

import io.metersphere.api.dto.scenario.ApiScenarioDetail;
import io.metersphere.api.service.scenario.ApiScenarioBatchRunService;
import io.metersphere.sdk.dto.api.task.ApiRunModeConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ExecTask implements Runnable {
    private ApiScenarioBatchRunService apiScenarioBatchRunService;
    private ApiScenarioDetail detail;
    private Map<String, String> scenarioReportMap;
    private ApiRunModeConfigDTO runModeConfig;

    @Override
    public void run() {
        apiScenarioBatchRunService.execute(detail, scenarioReportMap, runModeConfig);
    }
}
