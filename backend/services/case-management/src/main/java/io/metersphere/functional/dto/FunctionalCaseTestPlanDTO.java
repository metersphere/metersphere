package io.metersphere.functional.dto;

import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseTestPlanDTO extends TestPlanFunctionalCase {

    @Schema(description = "测试计划ID")
    private String num;

    @Schema(description = "所属项目")
    private String projectName;

    @Schema(description = "计划状态")
    private String planStatus;
}
