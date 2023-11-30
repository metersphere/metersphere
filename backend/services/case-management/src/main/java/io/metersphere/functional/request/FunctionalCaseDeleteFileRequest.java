package io.metersphere.functional.request;

import io.metersphere.functional.dto.FunctionalCaseAttachmentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseDeleteFileRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "附件信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private FunctionalCaseAttachmentDTO attachment;

    @Schema(description = "用例id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseId;

    @Schema(description = "项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
}
