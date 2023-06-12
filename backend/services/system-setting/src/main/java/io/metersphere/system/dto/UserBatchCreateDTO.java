package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserBatchCreateDTO {

    @Schema(title = "用户信息集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.info.not_empty}")
    List<@Valid User> userInfoList;

    @Schema(title = "组织Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.organizationId.not_blank}")
    List<@Valid @NotBlank(message = "{user.organizationId.not_blank}", groups = {Created.class, Updated.class}) String> organizationIdList;

    @Schema(title = "用户组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{user_role.id.not_blank}")
    List<@Valid @NotBlank(message = "{user_role.id.not_blank}", groups = {Created.class, Updated.class}) String> userRoleIdList;

    public void setCreateUserToList(String userSessionId) {
        userInfoList.forEach(user -> {
            user.setCreateUser(userSessionId);
            user.setUpdateUser(userSessionId);
        });
    }

    public void setUpdateUserToList(String userSessionId) {
        userInfoList.forEach(user -> {
            user.setUpdateUser(userSessionId);
        });
    }
}

