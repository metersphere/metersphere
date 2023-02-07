package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginInfoDTO implements Serializable {

    private String pluginId;

    private String sourcePath;
    
}
