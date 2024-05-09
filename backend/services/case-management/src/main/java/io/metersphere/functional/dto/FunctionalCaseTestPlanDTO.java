package io.metersphere.functional.dto;

import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseTestPlanDTO extends TestPlanFunctionalCase {

    @Schema(description = "测试计划ID")
    private Long testPlanNum;

    @Schema(description = "测试计划名称")
    private String testPlanName;

    @Schema(description = "所属项目名称")
    private String projectName;

    @Schema(description = "计划状态")
    private String planStatus;
}
