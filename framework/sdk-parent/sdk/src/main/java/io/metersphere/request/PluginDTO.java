package io.metersphere.request;

import io.metersphere.base.domain.Plugin;
import lombok.Data;

@Data
public class PluginDTO extends Plugin {
    private Boolean license;
}
