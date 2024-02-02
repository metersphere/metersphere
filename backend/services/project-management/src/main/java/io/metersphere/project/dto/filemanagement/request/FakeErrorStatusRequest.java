package io.metersphere.project.dto.filemanagement.request;

import io.metersphere.system.dto.table.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FakeErrorStatusRequest extends TableBatchProcessDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description =  "是否禁用")
    private Boolean enable;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.project_id.not_blank}")
    private String projectId;
}
