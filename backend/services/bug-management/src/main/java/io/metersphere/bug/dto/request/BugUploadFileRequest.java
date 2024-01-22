package io.metersphere.bug.dto.request;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BugUploadFileRequest implements Serializable {

    @Schema(description = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}")
    private String bugId;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}")
    private String projectId;

    @Schema(description = "不勾选的ID")
    private List<String> excludeIds;

    @Schema(description = "勾选的ID")
    @Valid
    private List<
            @NotBlank(message = "{id must not be blank}", groups = {Created.class, Updated.class})
                    String
            > selectIds = new ArrayList<>();

    @Schema(description = "是否全选", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean selectAll;

    @Schema(description = "模块ID(根据模块树查询时要把当前节点以及子节点都放在这里。)")
    private List<String> moduleIds;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description =  "关键字")
    private String keyword;
}
