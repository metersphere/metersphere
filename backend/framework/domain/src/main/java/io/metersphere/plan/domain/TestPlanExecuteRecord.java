package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanExecuteRecord implements Serializable {
    @Schema(description =  "测试计划执行记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execute_record.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_execute_record.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execute_record.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_execute_record.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description =  "执行记录名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execute_record.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{test_plan_execute_record.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description =  "执行状态;RUNNING/SUCCESS/ERROR/UNEXECUTE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execute_record.status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_execute_record.status.length_range}", groups = {Created.class, Updated.class})
    private String status;

    @Schema(description =  "触发类型;触发类型：SINGLE/BATCH/SCHEDULE/JENKINS")
    private String triggerMode;

    @Schema(description =  "创建人(执行人/触发人)")
    private String createUser;

    @Schema(description =  "创建时间(开始时间)")
    private Long createTime;

    @Schema(description =  "结束时间")
    private Long endTime;

    @Schema(description =  "用例总数量")
    private Integer caseCount;

    @Schema(description =  "执行率")
    private Double executeRate;

    @Schema(description =  "通过率")
    private Double passRate;

    @Schema(description =  "执行配置")
    private String runConfig;

    private static final long serialVersionUID = 1L;
}