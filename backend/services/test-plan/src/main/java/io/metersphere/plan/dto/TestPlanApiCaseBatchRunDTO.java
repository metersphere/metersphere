package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class TestPlanApiCaseBatchRunDTO {
    private String id;
    private String name;
    private String apiCaseId;
    private String environmentId;
    private String testPlanCollectionId;
}
