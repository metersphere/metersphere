package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jianxing
 */
@Data
@Schema(title = "用户组与用户的关联关系DTO")
public class UserRoleRelationUserDTO {

    @Schema(title = "关联关系ID")
    private String id;

    @Schema(title = "用户ID")
    private String userId;

    @Schema(title = "用户名")
    private String name;

    @Schema(title = "用户邮箱")
    private String email;

    @Schema(title = "手机号")
    private String phone;
}
