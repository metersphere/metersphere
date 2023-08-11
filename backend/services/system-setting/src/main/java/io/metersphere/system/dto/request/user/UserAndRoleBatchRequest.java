package io.metersphere.system.dto.request.user;

import io.metersphere.system.dto.request.UserBaseBatchRequest;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserAndRoleBatchRequest extends UserBaseBatchRequest {

    @Schema(description =  "组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class})
    @Valid
    private List<
            @NotBlank(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class, Updated.class})
            @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
                    String
            > roleIds;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class})
    @Valid
    private List<
            @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class, Updated.class})
            @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
                    String
            > userIds;
}
