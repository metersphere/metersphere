package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanResponse extends TestPlanStatisticsResponse {
    @Schema(description = "父Id")
    private String parent;
    
    @Schema(description = "项目ID")
    private String projectId;
    @Schema(description = "测试计划编号")
    private long num;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "测试计划类型 测试计划/测试计划组")
    private String type;
    @Schema(description = "标签")
    private List<String> tags;
    @Schema(description = "定时任务")
    private String schedule;
    @Schema(description = "创建人")
    private String createUser;
    @Schema(description = "创建人名称")
    private String createUserName;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "模块")
    private String moduleName;
    @Schema(description = "模块Id")
    private String moduleId;
    @Schema(description = "测试计划组内的测试计划")
    List<TestPlanResponse> children;
    @Schema(description = "组内计划数量")
    private Integer childrenCount;

    @Schema(description = "计划开始时间")
    private Long plannedStartTime;

    @Schema(description = "计划结束时间")
    private Long plannedEndTime;

    @Schema(description = "实际开始时间")
    private Long actualStartTime;

    @Schema(description = "实际结束时间")
    private Long actualEndTime;
    
    @Schema(description = "测试计划组Id")
    private String groupId;

    @Schema(description = "描述")
    private String description;

    private long pos;
}
