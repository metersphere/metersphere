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
public class UserMaintainRequest {
    @Schema(title = "用户信息集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(groups = {Created.class, Updated.class}, message = "{user.info.not_empty}")
    List<@Valid User> userInfoList;
    @Schema(title = "组织Id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {Created.class, Updated.class}, message = "{user.organizationId.not_blank}")
    String organizationId;
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(groups = {Created.class, Updated.class}, message = "{user.projectId.not_blank}")
    String projectId;

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

