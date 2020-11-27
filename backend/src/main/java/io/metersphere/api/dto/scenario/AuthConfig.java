package io.metersphere.api.dto.scenario;

import lombok.Data;

@Data
public class AuthConfig {
    private String verification;
    private String username;
    private String password;

}
