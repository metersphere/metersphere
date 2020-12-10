package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveApiPlanRequest {
    private List<String> planIds;
    private List<String> apiIds;
    private List<String> scenarioIds;
}
