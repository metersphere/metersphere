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

@Schema(title = "测试计划报告")
@Table("test_plan_report")
@Data
public class TestPlanReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_report.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_report.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanId;

    @Size(min = 1, max = 128, message = "{test_plan_report.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.name.not_blank}", groups = {Created.class})
    @Schema(title = "报告名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(min = 1, max = 50, message = "{test_plan_report.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_report.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "修改人")
    private String updateUser;


    @Schema(title = "更新时间")
    private Long updateTime;


    @Schema(title = "开始时间")
    private Long startTime;


    @Schema(title = "结束时间")
    private Long endTime;


    @Schema(title = "用例数量")
    private Long caseCount;


    @Schema(title = "执行率")
    private Double executeRate;


    @Schema(title = "通过率")
    private Double passRate;


}