package io.metersphere.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginConfigDTO {

    private List<PluginInfoDTO> pluginDTOS;
    private Map<String, Object> config;
}
