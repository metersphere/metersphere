package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestPlanScenarioStepCountDTO {
    private int scenarioStepTotal;
    private int scenarioStepSuccess;
    private int scenarioStepError;
    private int scenarioStepErrorReport;
    private List<String> underwayIds = new ArrayList<>();
    private int scenarioStepUnderway;
    private int scenarioStepUnExecute;
}

