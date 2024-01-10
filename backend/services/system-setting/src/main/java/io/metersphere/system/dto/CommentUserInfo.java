package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommentUserInfo{

    @Schema(description =  "用户ID")
    private String id;

    @Schema(description =  "用户名")
    private String name;

    @Schema(description =  "用户邮箱")
    private String email;

    @Schema(description = "用户头像")
    private String avatar;
}
