package io.metersphere.project.request.filemanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FileUpdateRequest {
    @Schema(description = "文件Id")
    @NotBlank(message = "{file_metadata.id.not_blank}")
    private String id;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "标签")
    private List<
            @NotBlank
                    String> tags;

    @Schema(description = "文件描述")
    private String description;

    @Schema(description = "模块ID")
    private String moduleId;
}
