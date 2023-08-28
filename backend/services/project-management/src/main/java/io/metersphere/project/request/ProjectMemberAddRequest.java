package io.metersphere.project.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectMemberAddRequest implements Serializable {

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;

    @Schema(description =  "用户ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.id.not_blank}")
    private List<String> userIds;

    @Schema(description =  "用户组ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user_role.id.not_blank}")
    private List<String> roleIds;
}
