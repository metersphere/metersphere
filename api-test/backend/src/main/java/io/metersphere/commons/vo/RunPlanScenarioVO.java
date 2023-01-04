package io.metersphere.commons.vo;

import io.metersphere.api.dto.plan.TestPlanApiScenarioInfoDTO;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public class RunPlanScenarioVO {
    private Map<String, TestPlanApiScenarioInfoDTO> testPlanScenarioMap;

    private Map<String, ApiScenarioWithBLOBs> scenarioMap;

    public RunPlanScenarioVO() {
        this.testPlanScenarioMap = new LinkedHashMap<>();
        this.scenarioMap = new LinkedHashMap<>();
    }
}
