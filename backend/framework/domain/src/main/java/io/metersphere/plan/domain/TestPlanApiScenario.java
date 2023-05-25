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

@Schema(title = "测试计划关联场景用例")
@Table("test_plan_api_scenario")
@Data
public class TestPlanApiScenario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_api_scenario.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_api_scenario.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanId;

    @Size(min = 1, max = 255, message = "{test_plan_api_scenario.api_scenario_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.api_scenario_id.not_blank}", groups = {Created.class})
    @Schema(title = "场景ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiScenarioId;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 100, message = "{test_plan_api_scenario.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_api_scenario.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;


    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;


    @Schema(title = "环境类型", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentType;


    @Schema(title = "所属环境", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environment;


    @Schema(title = "环境组ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String environmentGroupId;

}