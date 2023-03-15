package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Monitor {
    private String name;
    private String ip;
    private String port;
    private String description;
    private List<MonitorItem> monitorConfig;
}
