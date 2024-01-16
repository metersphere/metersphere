package io.metersphere.plan.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceSortRequest {
    @Schema(description = "测试计划ID")
    @NotBlank(message = "{test_plan.id.not_blank}")
    private String testPlanId;

    @Schema(description = "拖拽的节点ID")
    private String dragNodeId;

    @Schema(description = "目标节点")
    private String dropNodeId;

    @Schema(description = "放入的位置（取值：-1，,0，,1。  -1：dropNodeId节点之前。 0:dropNodeId节点内。 1：dropNodeId节点后）", requiredMode = Schema.RequiredMode.REQUIRED)
    private int dropPosition;

}

