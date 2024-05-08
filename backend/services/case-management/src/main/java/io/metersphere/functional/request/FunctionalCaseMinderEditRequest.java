package io.metersphere.functional.request;

import io.metersphere.functional.constants.MinderLabel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class FunctionalCaseMinderEditRequest{

    @Schema(description = "用例id/模块ID（其他类型比如前置给用例ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.id.not_blank}")
    private String id;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "资源名称（更新节点的名称包括前置的编辑）")
    private String name;

    @Schema(description = "步骤描述及其预期结果的排序（更新步骤描述时必填）")
    private Long pos;

    @Schema(description = "资源类型")
    private MinderLabel type;

    @Schema(description = "用例等级（只更新用例的等级时传）")
    private String priority;

}
