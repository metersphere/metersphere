package io.metersphere.project.dto.filemanagement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FileAssociationDeleteRequest {
    @Schema(description = "要删除的id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{file.association.source.not.exist}")
    List<@Valid @NotBlank(message = "{file.association.source.not.exist}") String> associationIds;

    @Schema(description = "项目Id")
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;
}
