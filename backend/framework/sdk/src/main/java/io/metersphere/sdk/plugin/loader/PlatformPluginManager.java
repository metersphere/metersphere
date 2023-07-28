package io.metersphere.sdk.plugin.loader;

import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.plugin.platform.dto.PlatformRequest;

/**
 * @author jianxing.chen
 */
public class PlatformPluginManager extends PluginManager {



    /**
     * 获取对应插件的 Platform 对象
     * @param pluginId 插件ID
     * @param request
     * @return
     */
    public Platform getPlatform(String pluginId, PlatformRequest request) {
        return getImplInstance(pluginId, Platform.class, request);
    }

}
