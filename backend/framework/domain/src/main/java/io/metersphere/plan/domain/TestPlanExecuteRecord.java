package io.metersphere.plan.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "测试计划执行记录")
@Table("test_plan_execute_record")
@Data
public class TestPlanExecuteRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_execute_record.id.not_blank}", groups = {Updated.class})
    @Schema(title = "测试计划执行记录ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_execute_record.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 255, message = "{test_plan_execute_record.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.name.not_blank}", groups = {Created.class})
    @Schema(title = "执行记录名称", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_execute_record.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_execute_record.status.not_blank}", groups = {Created.class})
    @Schema(title = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String status;


    @Schema(title = "触发类型", allowableValues = "range[1, 50]")
    private String triggerMode;


    @Schema(title = "创建人(执行人/触发人)", allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "创建时间(开始时间)")
    private Long createTime;


    @Schema(title = "结束时间")
    private Long endTime;


    @Schema(title = "用例总数量")
    private Integer caseCount;


    @Schema(title = "执行率", allowableValues = "range[1, 22]")
    private Double executeRate;


    @Schema(title = "通过率", allowableValues = "range[1, 22]")
    private Double passRate;


}