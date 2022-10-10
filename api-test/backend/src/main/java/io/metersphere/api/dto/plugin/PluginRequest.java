package io.metersphere.api.dto.plugin;

import lombok.Data;

@Data
public class PluginRequest {
    private String entry;
    private String request;
}
