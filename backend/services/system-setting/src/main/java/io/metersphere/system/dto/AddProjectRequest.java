package io.metersphere.system.dto;

import io.metersphere.system.dto.sdk.ProjectBaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AddProjectRequest extends ProjectBaseRequest {

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 1, max = 50, message = "{project.id.length_range}")
    private String id;
}
