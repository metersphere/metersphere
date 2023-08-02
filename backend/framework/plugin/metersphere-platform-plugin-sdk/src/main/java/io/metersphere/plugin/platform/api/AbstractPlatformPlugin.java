package io.metersphere.plugin.platform.api;

import io.metersphere.plugin.sdk.api.AbstractMsPlugin;

public abstract class AbstractPlatformPlugin extends AbstractMsPlugin {
    private static final String PLATFORM_PLUGIN_TYPE = "PLATFORM";
    @Override
    public String getType() {
        return PLATFORM_PLUGIN_TYPE;
    }
}
