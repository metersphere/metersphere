package io.metersphere.dto;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.track.dto.UiRunModeConfigDTO;
import lombok.Data;

@Data
public class RunUiScenarioRequest extends RunScenarioRequest {
    private UiRunModeConfigDTO uiConfig;
}
