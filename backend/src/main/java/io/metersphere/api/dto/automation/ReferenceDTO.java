package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenario;
import io.metersphere.track.dto.TestPlanDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReferenceDTO {
    List<ApiScenario> scenarioList;

    List<TestPlanDTO> testPlanList;
}
