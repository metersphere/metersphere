package io.metersphere.api.dto.definition.request.sampler.dubbo;

import lombok.Data;

@Data
public class MsRegistryCenter {
    private String protocol;
    private String group;
    private String username;
    private String address;
    private String password;
    private String timeout;
}
