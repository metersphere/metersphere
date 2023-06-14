package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProjectMemberRequest {
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(title = "用户ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.ids.not_blank}")
    private List<String> userIds;

    @Schema(title = "当前用户")
    private String createUser;

}
