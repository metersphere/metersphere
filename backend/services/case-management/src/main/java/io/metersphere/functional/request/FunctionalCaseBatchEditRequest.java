package io.metersphere.functional.request;

import io.metersphere.functional.dto.BaseFunctionalCaseBatchDTO;
import io.metersphere.functional.dto.CaseCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCaseBatchEditRequest extends BaseFunctionalCaseBatchDTO {


    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;

    @Schema(description = "是否追加")
    private boolean append;

    @Schema(description = "是否清空")
    private boolean clear;

    @Schema(description = "标签")
    private List<String> tags;


    @Schema(description = "自定义字段")
    private CaseCustomFieldDTO customField;
}