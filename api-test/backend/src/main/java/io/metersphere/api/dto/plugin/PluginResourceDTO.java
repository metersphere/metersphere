package io.metersphere.api.dto.plugin;

import io.metersphere.plugin.core.ui.PluginResource;
import lombok.Data;

@Data
public class PluginResourceDTO extends PluginResource {
    private String entry;
}
