package io.metersphere.project.request.filemanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileBatchProcessDTO {
    @Schema(description = "不处理的ID")
    List<String> excludeIds;

    @Schema(description = "项目ID")
    @NotBlank(message = "{id must not be blank}")
    private String projectId;

    @Schema(description = "选择的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<
            @NotBlank(message = "{id must not be blank}")
                    String
            > selectIds = new ArrayList<>();

    @Schema(description = "是否选择所有数据")
    private boolean selectAll;

    @Schema(description = "文件类型")
    private List<String> fileTypes;

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "模块ID")
    private List<String> moduleIds;
}
