package io.metersphere.api.dto.delimit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiComputeResult {
    private String apiDelimitId;
    private String caseTotal;
    private String status;
    private String passRate;
}
