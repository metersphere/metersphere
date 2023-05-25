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

@Schema(title = "测试计划配置")
@Table("test_plan_config")
@Data
public class TestPlanConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_config.test_plan_id.not_blank}", groups = {Updated.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanId;


    @Schema(title = "运行模式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String runModeConfig;

    @Size(min = 1, max = 1, message = "{test_plan_config.automatic_status_update.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.automatic_status_update.not_blank}", groups = {Created.class})
    @Schema(title = "是否自定更新功能用例状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean automaticStatusUpdate;

    @Size(min = 1, max = 1, message = "{test_plan_config.repeat_case.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.repeat_case.not_blank}", groups = {Created.class})
    @Schema(title = "是否允许重复添加用例", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean repeatCase;

    @Size(min = 1, max = 3, message = "{test_plan_config.pass_threshold.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_config.pass_threshold.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划通过阈值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer passThreshold;

}