package io.metersphere.api.dto.definition;

import lombok.Data;

@Data
public class SwaggerBasicAuth {
    private Boolean authSwitch = false;
    private String userName;
    private String password;

    //新增token设置。放在这里也是因为token是身份验证的一种。
    private String token;
}
