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

@Schema(title = "测试计划关联功能用例")
@Table("test_plan_function_case")
@Data
public class TestPlanFunctionCase implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{test_plan_function_case.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{test_plan_function_case.test_plan_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_function_case.test_plan_id.not_blank}", groups = {Created.class})
    @Schema(title = "测试计划ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String testPlanId;

    @Size(min = 1, max = 50, message = "{test_plan_function_case.function_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_function_case.function_case_id.not_blank}", groups = {Created.class})
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String functionCaseId;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Size(min = 1, max = 50, message = "{test_plan_function_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{test_plan_function_case.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "range[1, 50]")
    private String createUser;


    @Schema(title = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;


}