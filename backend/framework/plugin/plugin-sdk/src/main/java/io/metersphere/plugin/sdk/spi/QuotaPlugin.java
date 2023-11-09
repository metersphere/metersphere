package io.metersphere.plugin.sdk.spi;

import io.metersphere.plugin.sdk.util.MSPluginException;

public abstract class QuotaPlugin extends AbstractMsPlugin {
    public abstract void interceptor(Object pjp) throws MSPluginException;
}
