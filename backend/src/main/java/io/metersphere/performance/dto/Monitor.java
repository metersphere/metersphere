package io.metersphere.performance.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Monitor {
    private String name;
    private String environmentId;
    private String environmentName;
    private String ip;
    private Integer port;
    private String description;
    private String monitorStatus;
}
