package io.metersphere.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author jianxing
 */
@Data
@Schema(title = "用户基础信息")
public class GlobalUserRoleUserDTO {

    @Schema(title = "用户名")
    private String name;

    @Schema(title = "用户邮箱")
    private String email;

    @Schema(title = "手机号")
    private String phone;
}
