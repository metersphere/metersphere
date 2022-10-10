package io.metersphere.dto;

import io.metersphere.plan.dto.ApiTestCaseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanApiCaseDTO extends ApiTestCaseDTO {
    private String environmentId;
    private String caseId;
    private String userId;
    private String creatorName;
    private String principalName;
    private String updateName;
    private String environmentName;
}
