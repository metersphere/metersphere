package io.metersphere.plugin.platform.api;

import io.metersphere.plugin.platform.dto.PlatformRequest;
import io.metersphere.plugin.sdk.util.JSON;
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
        return JSON.parseObject(integrationConfig, clazz);
    }

    public String getPluginId() {
        return request.getPluginId();
    }
}
