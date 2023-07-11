package io.metersphere.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrganizationUserRoleMemberEditRequest implements Serializable {

    @Schema(title = "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}")
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}")
    private String userRoleId;

    @Schema(title = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    @Size(min = 1, max = 50, message = "{organization.id.length_range}")
    private String organizationId;

    @Schema(title = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user.id.not_blank}")
    @Size(min = 1, max = 50, message = "{user.id.length_range}")
    private String userId;
}
