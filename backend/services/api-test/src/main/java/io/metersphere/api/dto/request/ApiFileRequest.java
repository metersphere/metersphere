package io.metersphere.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiFileRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review.project_id.not_blank}")
    private String projectId;

    @Schema(description = "资源id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{case_review_functional_case.case_id.not_blank}")
    private String sourceId;

    @Schema(description = "文件id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}")
    private String fileId;

    @Schema(description = "是否本地", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean local;

    @Schema(description = "文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

}
