package io.metersphere.dto;

import io.metersphere.api.dto.automation.RunScenarioRequest;
import io.metersphere.track.dto.UiRunModeConfigDTO;
import lombok.Data;

import java.util.List;

@Data
public class RunUiScenarioRequest extends RunScenarioRequest {
    private UiRunModeConfigDTO uiConfig;

    @Override
    public void setIds(List<String> ids) {
        super.setIds(ids);
    }
}
