package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class AttachmentTransferRequest extends FunctionalCaseFileRequest{


    @Schema(description = "转存的模块id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case.module_id.not_blank}")
    private String moduleId;


    @Schema(description = "文件别名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;
}
