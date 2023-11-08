package io.metersphere.system.dto;

import io.metersphere.system.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CommentUserInfo extends User {

    @Schema(description = "用户头像")
    private String avatar;
}
