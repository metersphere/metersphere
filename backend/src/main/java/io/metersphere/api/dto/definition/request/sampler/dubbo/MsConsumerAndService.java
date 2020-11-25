package io.metersphere.api.dto.definition.request.sampler.dubbo;

import lombok.Data;

@Data
public class MsConsumerAndService {
    private String timeout;
    private String version;
    private String retries;
    private String cluster;
    private String group;
    private String connections;
    private String async;
    private String loadBalance;
}
