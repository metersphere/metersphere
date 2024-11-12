package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class TestPlanApiCaseBatchRunDTO {
    private String id;
    private String name;
    private String testPlanCollectionId;
    private String caseId;
}
