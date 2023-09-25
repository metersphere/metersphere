package io.metersphere.project.request.filemanagement;

import io.metersphere.sdk.dto.TableBatchProcessDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FileBatchProcessDTO extends TableBatchProcessDTO {

    @Schema(description = "项目ID")
    @NotBlank(message = "{id must not be blank}")
    private String projectId;

    @Schema(description = "文件类型")
    private List<String> fileTypes;

    @Schema(description = "模块ID")
    private List<String> moduleIds;
}
