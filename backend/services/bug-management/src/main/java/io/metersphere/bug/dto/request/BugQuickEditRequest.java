package io.metersphere.bug.dto.request;

import io.metersphere.bug.dto.response.BugCustomFieldDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugQuickEditRequest {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}")
    @Size(min = 1, max = 50, message = "{bug.id.length_range}")
    private String id;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}")
    @Size(min = 1, max = 50, message = "{bug.project_id.length_range}")
    private String projectId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.template_id.not_blank}")
    @Size(min = 1, max = 50, message = "{bug.template_id.length_range}")
    private String templateId;

    @Schema(description = "处理人")
    private String handleUser;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "缺陷内容")
    private String description;

    @Schema(description = "自定义字段集合")
    private List<BugCustomFieldDTO> customFields;
}
