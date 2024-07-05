package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInviteRequest {
    @Schema(description = "邀请用户的Email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.email.invalid}")
    List<@Valid
    @NotBlank(message = "{user.email.invalid}")
    @Size(min = 1, max = 64, message = "{user.email.length_range}")
    @Email(message = "{user.email.invalid}")
            String> inviteEmails;
    @Schema(description = "邀请用户所属用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user_role.id.not_blank}")
    List<@Valid @NotBlank(message = "{user_role.id.not_blank}") String> userRoleIds;

    /**
     * 组织ID
     */
    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String organizationId;

    /**
     * 组织ID
     */
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;
}
