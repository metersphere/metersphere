package io.metersphere.xpack.resourcepool.engine.provider;

import lombok.Data;

@Data
public class ClientCredential {

    private String masterUrl;
    private String token;
    private Integer maxConcurrency;
    private String namespace;
    private String nodeSelector;
    private Integer podThreadLimit;
    private String apiImage;
    private String deployName;
    private String jobTemplate;
}
