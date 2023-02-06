package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginDTO implements Serializable {

    private String pluginId;

    private String sourcePath;
    
}
