package io.metersphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPlanApiDTO extends TestPlanApiCaseDTO {
    private String response;
    private String reportId;
}
