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
public class FunctionalCaseFileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "用例id",requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseId;

    @Schema(description = "文件id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}")
    private String fileId;

    @Schema(description = "是否本地",requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean local;


}
