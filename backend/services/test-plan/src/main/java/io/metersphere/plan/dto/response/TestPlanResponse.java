package io.metersphere.plan.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanResponse {
    //：ID、计划名称、计划状态、测试进度、通过率、用例数、定时任务、bug数、创建人、创建时间、模块
    @Schema(description = "测试计划ID")
    private String id;
    @Schema(description = "测试计划编号")
    private long num;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "测试进度")
    private String testProgress;
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
    @Schema(description = "定时任务")
    private String schedule;
    @Schema(description = "创建人")
    private String createUser;
    @Schema(description = "创建时间")
    private String createTime;
    @Schema(description = "模块")
    private String moduleName;
    @Schema(description = "模块Id")
    private String moduleId;
    @Schema(description = "类型（测试计划组/测试计划）")
    private String type;
}
