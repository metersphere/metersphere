package io.metersphere.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrganizationUserRoleMemberEditRequest implements Serializable {

    @Schema(description =  "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{user_role.id.not_blank}")
    @Size(min = 1, max = 50, message = "{user_role.id.length_range}")
    private String userRoleId;

    @Schema(description =  "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}")
    @Size(min = 1, max = 50, message = "{organization.id.length_range}")
    private String organizationId;

    @Schema(description =  "成员ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.id.not_blank}")
    private List<String> userIds;
}
