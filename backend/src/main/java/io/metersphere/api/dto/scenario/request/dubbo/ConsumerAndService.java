package io.metersphere.api.dto.scenario.request.dubbo;

import lombok.Data;

@Data
public class ConsumerAndService {
    private String timeout;
    private String version;
    private String retries;
    private String cluster;
    private String group;
    private String connections;
    private String async;
    private String loadBalance;
}
