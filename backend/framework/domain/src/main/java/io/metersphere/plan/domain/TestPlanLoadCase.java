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

@Schema(title = "测试计划关联性能测试用例")
@Table("test_plan_load_case")
@Data
public class TestPlanLoadCase implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_load_case.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.load_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.load_case_id.not_blank}", groups = {Created.class})
    @Schema(title = "性能用例ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String loadCaseId;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan_load_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_load_case.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "所用测试资源池ID", allowableValues = "range[1, 50]")
    private String testResourcePoolId;


    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;


    @Schema(title = "压力配置")
    private String loadConfiguration;


    @Schema(title = "高级配置")
    private String advancedConfiguration;


}