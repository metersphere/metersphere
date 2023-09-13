package io.metersphere.system.dto;

import lombok.Data;

@Data
public class PluginNotifiedDTO {
    private String operate;
    private String pluginId;
    private String fileName;
}
