package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseImportRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.project_id.not_blank}")
    private String projectId;


    @Schema(description = "版本ID")
    private String versionId;

    @Schema(description = "是否覆盖原用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.cover.not_blank}")
    private boolean cover;

    @Schema(description = "导入数量")
    private String count;
}
