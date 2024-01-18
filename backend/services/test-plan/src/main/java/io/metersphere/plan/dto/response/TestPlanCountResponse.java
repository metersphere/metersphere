package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanCountResponse {
    @Schema(description = "测试计划ID")
    private String id;
    @Schema(description = "通过率")
    private String passRate;
    @Schema(description = "功能用例数")
    private long functionalCaseCount = -1;
    @Schema(description = "接口用例数")
    private long apiCaseCount = -1;
    @Schema(description = "接口场景数")
    private long apiScenarioCount = -1;
    @Schema(description = "Bug数量")
    private long bugCount = -1;
    @Schema(description = "测试进度")
    private String testProgress;
}
