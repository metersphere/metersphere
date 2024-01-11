package io.metersphere.api.dto.definition;

import lombok.Data;

@Data
public class SwaggerBasicAuth {
    private Boolean authSwitch = false;
    private String userName;
    private String password;
}
