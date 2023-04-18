package io.metersphere.sdk.plugin.loader;

import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.plugin.platform.api.PluginMetaInfo;
import io.metersphere.plugin.platform.dto.PlatformRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jianxing.chen
 */
public class PlatformPluginManager extends PluginManager {

    /**
     * 获取当前加载的插件元数据列表
     * @return
     */
    public List<PluginMetaInfo> getPluginMetaInfoList() {
        List<PluginMetaInfo> platFormOptions = new ArrayList<>();
        for (String pluginId : getClassLoaderMap().keySet()) {
           platFormOptions.add(getImplInstance(pluginId, PluginMetaInfo.class));
        }
        return platFormOptions;
    }

    /**
     * 获取当前插件的元数据
     * @param pluginId 插件ID
     * @return
     */
    public PluginMetaInfo getPluginMetaInfo(String pluginId) {
        return getImplInstance(pluginId, PluginMetaInfo.class);
    }

    /**
     * 获取对应插件的 Platform 对象
     * @param pluginId 插件ID
     * @param request
     * @return
     */
    public Platform getPlatform(String pluginId, PlatformRequest request) {
        return getImplInstance(pluginId, Platform.class, request);
    }

    /**
     * 获取对应插件的 Platform 对象
     * @param key 插件 key
     * @param request
     * @return
     */
    public Platform getPlatformByKey(String key, PlatformRequest request) {
        for (String pluginId : getClassLoaderMap().keySet()) {
            // 查找对应 key 的插件
            PluginMetaInfo pluginMetaInfo = getPluginMetaInfo(pluginId);
            if (StringUtils.equals(pluginMetaInfo.getKey(), key)) {
                return getPlatform(pluginId, request);
            }
        }
        return null;
    }

    /**
     * 获取当前插件的元数据
     * @param key 插件key
     * @return
     */
    public PluginMetaInfo getPluginMetaInfoByKey(String key) {
        for (String pluginId : getClassLoaderMap().keySet()) {
            // 查找对应 key 的插件
            PluginMetaInfo pluginMetaInfo = getPluginMetaInfo(pluginId);
            if (StringUtils.equals(pluginMetaInfo.getKey(), key)) {
                return pluginMetaInfo;
            }
        }
        return null;
    }
}
