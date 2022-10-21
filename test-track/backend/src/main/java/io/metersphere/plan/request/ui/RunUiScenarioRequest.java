package io.metersphere.plan.request.ui;

import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.plan.request.api.RunScenarioRequest;
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
