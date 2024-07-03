package io.metersphere.plan.dto;

import lombok.Data;

@Data
public class TestPlanResourceExecResultDTO {
    private String testPlanId;
    private String execResult;
    private String testPlanGroupId;
}
