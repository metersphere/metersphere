package io.metersphere.functional.request;

import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseChangeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用例id(新增的时候前端传UUid，更新的时候必填)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.template_id.not_blank}")
    private String templateId;

    @Schema(description = "操作类型（新增(ADD)/更新(UPDATE)）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.relation.not_blank}")
    private String type;

    @Schema(description = "用例名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.name.not_blank}")
    private String name;

    @Schema(description = "模块id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.module_id.not_blank}")
    private String moduleId;

    @Schema(description = "用例等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.name.not_blank}")
    private int priority = 0;

    @Schema(description = "移动方式（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moveMode;

    @Schema(description = "移动目标（节点移动或新增时需要）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetId;

    @Schema(description = "前置条件", defaultValue = "")
    private String prerequisite;

    @Schema(description = "编辑模式", allowableValues = {"STEP", "TEXT"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.case_edit_type.not_blank}")
    private String caseEditType;

    @Schema(description = "用例步骤", defaultValue = "")
    private String steps;

    @Schema(description = "步骤描述", defaultValue = "")
    private String textDescription;

    @Schema(description = "预期结果", defaultValue = "")
    private String expectedResult;

    @Schema(description = "备注", defaultValue = "")
    private String description;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "自定义字段集合")
    private List<CaseCustomFieldDTO> customFields;

}
