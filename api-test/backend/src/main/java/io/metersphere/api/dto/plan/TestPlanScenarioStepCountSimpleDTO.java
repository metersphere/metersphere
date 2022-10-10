package io.metersphere.api.dto.plan;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestPlanScenarioStepCountSimpleDTO {
    private TestPlanScenarioStepCountDTO stepCount;
    private int underwayStepsCounts ;
}

