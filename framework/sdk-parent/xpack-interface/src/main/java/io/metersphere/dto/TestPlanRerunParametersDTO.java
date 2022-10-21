package io.metersphere.dto;

import io.metersphere.base.domain.TestPlanReport;
import lombok.Data;

@Data
public class TestPlanRerunParametersDTO extends RerunParametersDTO {
    private TestPlanReport testPlanReport;
}
