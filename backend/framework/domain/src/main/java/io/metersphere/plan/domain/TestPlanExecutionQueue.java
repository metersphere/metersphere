package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanExecutionQueue implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execution_queue.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "执行批次;随机生成UUID作为执行批次", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execution_queue.execute_batch.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.execute_batch.length_range}", groups = {Created.class, Updated.class})
    private String executeBatch;

    @Schema(description =  "testPlanId", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execution_queue.test_plan_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanId;

    @Schema(description =  "测试计划执行记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execution_queue.test_plan_execute_record_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.test_plan_execute_record_id.length_range}", groups = {Created.class, Updated.class})
    private String testPlanExecuteRecordId;

    @Schema(description =  "执行模式;并行/串行", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_execution_queue.run_mode.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 20, message = "{test_plan_execution_queue.run_mode.length_range}", groups = {Created.class, Updated.class})
    private String runMode;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "排序")
    private Integer pos;

    private static final long serialVersionUID = 1L;
}