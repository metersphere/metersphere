package io.metersphere.api.dto;

import lombok.Data;

@Data
public class RegistryCenter {
    private String protocol;
    private String group;
    private String username;
    private String address;
    private String password;
    private String timeout;
}
