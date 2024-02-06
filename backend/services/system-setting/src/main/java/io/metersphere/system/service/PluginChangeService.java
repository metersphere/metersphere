package io.metersphere.system.service;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-06  20:47
 */
public interface PluginChangeService {
    /**
     * 插件时调用
     * @param pluginId 插件ID
     */
    void handlePluginLoad(String pluginId);
}
