package io.metersphere.plan.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "测试计划列表批量执行队列")
@TableName("test_plan_execution_queue")
@Data
public class TestPlanExecutionQueue implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{test_plan_execution_queue.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.execute_batch.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execution_queue.execute_batch.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行批次", required = true, allowableValues="range[1, 50]")
    private String executeBatch;

    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execution_queue.test_plan_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "testPlanId", required = true, allowableValues="range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_execution_queue.test_plan_execute_record_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execution_queue.test_plan_execute_record_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试计划执行记录ID", required = true, allowableValues="range[1, 50]")
    private String testPlanExecuteRecordId;

    @Size(min = 1, max = 20, message = "{test_plan_execution_queue.run_mode.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execution_queue.run_mode.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "执行模式", required = true, allowableValues="range[1, 20]")
    private String runMode;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "排序", dataType = "Integer")
    private Integer num;
}
