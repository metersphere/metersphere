package io.metersphere.system.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleBatchRelationRequest {
    /**
     * 权限ID集合
     */
    @Schema(description = "权限ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{organization.id.not_blank}")
    private List<String> roleIds;

    /**
     * 成员ID集合
     */
    @Schema(description = "成员ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user.id.not_blank}")
    private List<String> userIds;
}
