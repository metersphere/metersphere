package io.metersphere.sdk.dto;

import io.metersphere.system.domain.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserDTO extends User {

    @ApiModelProperty(name = "其他平台对接信息", required = false, allowableValues = "range[1, 2000]")
    private byte[] platformInfo;

    @ApiModelProperty(name = "UI本地调试地址", required = false, allowableValues = "range[1, 255]")
    private String seleniumServer;
}
