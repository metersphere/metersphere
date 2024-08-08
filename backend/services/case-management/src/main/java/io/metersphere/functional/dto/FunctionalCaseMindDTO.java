package io.metersphere.functional.dto;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionalCaseMindDTO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "caseID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case.id.length_range}", groups = {Created.class, Updated.class})
    private String caseId;

    @Schema(description = "模块ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.module_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.module_id.length_range}", groups = {Created.class, Updated.class})
    private String moduleId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.template_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "评审状态：未评审/评审中/通过/不通过/重新提审", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.review_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case.review_status.length_range}", groups = {Created.class, Updated.class})
    private String reviewStatus;

    @Schema(description =  "用例等级")
    private String priority;

    @Schema(description = "自定义排序，间隔5000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{functional_case.pos.not_blank}", groups = {Created.class})
    private Long pos;

    @Schema(description = "编辑模式：步骤模式/文本模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.case_edit_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case.case_edit_type.length_range}", groups = {Created.class, Updated.class})
    private String caseEditType;

    @Schema(description =  "用例步骤（JSON)，step_model 为 Step 时启用")
    private byte[] steps;

    @Schema(description =  "执行用例步骤（JSON)，step_model 为 Step 时启用")
    private byte[] executeSteps;

    @Schema(description =  "步骤描述，step_model 为 Text 时启用")
    private byte[] textDescription;

    @Schema(description =  "预期结果，step_model 为 Text  时启用")
    private byte[] expectedResult;

    @Schema(description =  "前置条件")
    private byte[] prerequisite;

    @Schema(description =  "备注")
    private byte[] description;

    @Schema(description =  "执行评论")
    private byte[] content;
}
