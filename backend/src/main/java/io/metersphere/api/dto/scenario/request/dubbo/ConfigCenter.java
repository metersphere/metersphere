package io.metersphere.api.dto.scenario.request.dubbo;

import lombok.Data;

@Data
public class ConfigCenter {
    private String protocol;
    private String group;
    private String namespace;
    private String username;
    private String address;
    private String password;
    private String timeout;
}
