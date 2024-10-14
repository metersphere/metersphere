package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class TestPlanApiScenarioBatchRunDTO {
    private String id;
    private String name;
    private String apiScenarioId;
    private String environmentId;
    private String testPlanCollectionId;
}
