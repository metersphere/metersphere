package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunCaseRequest {

    private String caseId;

    private String reportId;

    private String runMode;

    private String environmentId;

    private String testPlanId;
}
