package io.metersphere.project.request.filemanagement;

import io.metersphere.sdk.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class FileMetadataTableRequest extends BasePageRequest {
    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<String> moduleIds;
    
    @Schema(description = "文件类型")
    private List<String> fileTypes;

    @Schema(description = "项目ID")
    @NotBlank(message = "{id must not be blank}")
    private String projectId;
}
