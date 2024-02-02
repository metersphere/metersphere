package io.metersphere.project.dto.filemanagement.request;


import io.metersphere.system.dto.sdk.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wx
 */
@Getter
@Setter
public class FakeErrorRequest extends BasePageRequest {

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{fake_error.project_id.not_blank}")
    private String projectId;

}
