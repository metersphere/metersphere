package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiTestCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanApiCaseDTO extends ApiTestCaseDTO {
    private String environmentId;
    private String caseId;
    private String execResult;
    private String userId;
}
