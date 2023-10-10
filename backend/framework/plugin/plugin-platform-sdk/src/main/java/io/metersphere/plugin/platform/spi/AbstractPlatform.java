package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.dto.PlatformRequest;
import io.metersphere.plugin.sdk.util.PluginUtils;
import io.metersphere.plugin.sdk.util.MSPluginException;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPlatform implements Platform {
    protected PlatformRequest request;

    public AbstractPlatform(PlatformRequest request) {
        this.request = request;
    }

    public <T> T getIntegrationConfig(String integrationConfig, Class<T> clazz) {
        if (StringUtils.isBlank(integrationConfig)) {
            throw new MSPluginException("服务集成配置为空");
        }
        return PluginUtils.parseObject(integrationConfig, clazz);
    }

    public String getPluginId() {
        return request.getPluginId();
    }
}
