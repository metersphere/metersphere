package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanApiCaseDTO extends ApiTestCaseDTO {
    private String environmentId;
    private String caseId;
    private String execResult;
    private String userId;
    private String creatorName;
    private String principalName;
    private String updateName;
}
