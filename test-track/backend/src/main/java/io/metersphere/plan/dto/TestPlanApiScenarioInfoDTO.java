package io.metersphere.plan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanApiScenarioInfoDTO {
    private String id;
    private String apiScenarioId;
    private String environment;
    private String projectId;
    private String environmentType;
    private String environmentGroupId;
}
