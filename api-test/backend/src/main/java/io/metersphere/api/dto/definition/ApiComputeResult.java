package io.metersphere.api.dto.definition;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiComputeResult {
    private String apiDefinitionId;
    private int caseTotal;
    private String status;
    private String passRate;
    private int success;
    private int error;
}
