package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanAndGroupInfoDTO {
    @Schema(description = "计划id")
    private String id;
    @Schema(description = "计划名称")
    private String name;
    @Schema(description = "计划名称")
    private Long createTime;
    @Schema(description = "计划组ID")
    private String groupId;
    @Schema(description = "计划组名称")
    private String groupName;
    @Schema(description = "计划组创建时间")
    private Long groupCreateTime;
    @Schema(description = "项目ID")
    private String projectId;

}
