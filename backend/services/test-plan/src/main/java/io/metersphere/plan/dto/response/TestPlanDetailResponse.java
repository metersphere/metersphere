package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class TestPlanDetailResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "测试计划ID")
    private String id;

    @Schema(description = "测试计划组Id")
    private String groupId;
    @Schema(description = "测试计划组名称")
    private String groupName;

    @Schema(description = "计划开始时间")
    private Long plannedStartTime;

    @Schema(description = "计划结束时间")
    private Long plannedEndTime;

    @Schema(description = "描述;描述")
    private String description;

    @Schema(description = "是否自定更新功能用例状态")
    private Boolean automaticStatusUpdate;

    @Schema(description = "是否允许重复添加用例")
    private Boolean repeatCase;

    @Schema(description = "测试计划通过阈值;0-100")
    private Double passThreshold;

    @Schema(description = "是否开启测试规划")
    private Boolean testPlanning;


    @Schema(description = "测试计划名称/测试计划组名称")
    private String name;
    @Schema(description = "标签")
    private List<String> tags;
    @Schema(description = "模块")
    private String moduleName;
    @Schema(description = "模块Id")
    private String moduleId;
}
