package io.metersphere.plan.reuest.ui;

import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plan.reuest.api.RunScenarioRequest;
import lombok.Data;

import java.util.List;

@Data
public class RunUiScenarioRequest extends RunScenarioRequest {
    private RunModeConfigDTO uiConfig;

    @Override
    public void setIds(List<String> ids) {
        super.setIds(ids);
    }
}
