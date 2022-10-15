package io.metersphere.api.dto.scenario.environment.item;

import io.metersphere.commons.constants.ElementConstants;
import lombok.Data;

@Data
public class EnvAuthManager extends BaseEnvElement {
    private String type = ElementConstants.AUTH_MANAGER;
    private String clazzName = EnvAuthManager.class.getCanonicalName();
    private String username;
    private String password;
    private String url;
    private String realm;
    private String verification;
    private String mechanism;
    private String encrypt;
    private String domain;
    private String environment;
}
