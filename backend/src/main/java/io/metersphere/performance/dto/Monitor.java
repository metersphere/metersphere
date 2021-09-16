package io.metersphere.performance.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Monitor {
    private String name;
    private String ip;
    private Integer port;
    private String description;
    private List<MonitorItem> monitorConfig;
}
