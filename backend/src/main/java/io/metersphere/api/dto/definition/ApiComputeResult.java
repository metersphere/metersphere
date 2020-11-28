package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiComputeResult {
    private String apiDefinitionId;
    private String caseTotal;
    private String status;
    private String passRate;
}
