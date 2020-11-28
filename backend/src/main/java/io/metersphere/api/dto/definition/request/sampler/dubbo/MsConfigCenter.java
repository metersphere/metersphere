package io.metersphere.api.dto.definition.request.sampler.dubbo;

import lombok.Data;

@Data
public class MsConfigCenter {
    private String protocol;
    private String group;
    private String namespace;
    private String username;
    private String address;
    private String password;
    private String timeout;
}
