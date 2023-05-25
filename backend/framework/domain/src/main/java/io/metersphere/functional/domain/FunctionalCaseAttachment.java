package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例和附件的中间表")
@Table("functional_case_attachment")
@Data
public class FunctionalCaseAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{functional_case_attachment.functional_case_id.not_blank}", groups = {Updated.class})
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String functionalCaseId;

    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}", groups = {Updated.class})
    @Schema(title = "文件的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileId;

}
