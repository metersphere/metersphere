package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseTemplateExtend implements Serializable {
    @Schema(title = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_template_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "用例名称模板")
    private String caseName;

    @Schema(title = "编辑模式模板：步骤模式/文本模式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template_extend.step_model.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_template_extend.step_model.length_range}", groups = {Created.class, Updated.class})
    private String stepModel;

    @Schema(title = "前置条件模板")
    private String prerequisite;

    @Schema(title = "步骤描述模板")
    private String stepDescription;

    @Schema(title = "预期结果模板")
    private String expectedResult;

    @Schema(title = "实际结果模板")
    private String actualResult;

    @Schema(title = "用例步骤")
    private String steps;

    private static final long serialVersionUID = 1L;
}