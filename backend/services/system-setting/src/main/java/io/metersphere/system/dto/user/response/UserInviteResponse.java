package io.metersphere.system.dto.user.response;

import io.metersphere.system.domain.UserInvite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserInviteResponse {
    @Schema(description = "邀请记录ID")
    private List<String> inviteIds;

    public UserInviteResponse(List<UserInvite> userInvites) {
        this.inviteIds = userInvites.stream().map(UserInvite::getId).toList();
    }
}
