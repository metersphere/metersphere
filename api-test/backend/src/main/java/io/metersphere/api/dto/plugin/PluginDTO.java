package io.metersphere.api.dto.plugin;

import io.metersphere.base.domain.Plugin;
import lombok.Data;

@Data
public class PluginDTO extends Plugin {
    private Boolean license;
}
