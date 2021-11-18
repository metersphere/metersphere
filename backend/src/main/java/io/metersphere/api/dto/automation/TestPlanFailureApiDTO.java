package io.metersphere.api.dto.automation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanFailureApiDTO extends TestPlanApiCaseDTO {
    private String response;
    private String reportId;
}
