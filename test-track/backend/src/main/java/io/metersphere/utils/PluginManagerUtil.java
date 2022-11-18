package io.metersphere.utils;

import im.metersphere.plugin.loader.PluginManager;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.plugin.MinioStorageStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PluginManagerUtil {

    /**
     * 加载 jar
     * 并设置存储策略
     * @param id
     * @param pluginManager
     * @param inputStream
     */
    public static void loadPlugin(String id, PluginManager pluginManager, InputStream inputStream) {
        if (pluginManager == null) {
            pluginManager = new PluginManager();
        }
        // 加载 jar
        try {
            pluginManager.loadJar(id, inputStream, new MinioStorageStrategy(id));
        } catch (IOException e) {
            LogUtil.error("加载jar包失败: ", e);
            MSException.throwException("加载jar包失败: " + e.getMessage());
        }
    }

    /**
     * 加载插件
     */
    public static void loadPlugins(PluginManager pluginManager, List<PluginWithBLOBs> plugins) {
        plugins.forEach(plugin -> {
            String id = plugin.getId();
            MinioStorageStrategy minioStorageStrategy = new MinioStorageStrategy(id);
            InputStream inputStream = minioStorageStrategy.get(plugin.getSourceName());
            try {
                pluginManager.loadJar(id, inputStream, minioStorageStrategy);
            } catch (IOException e) {
                LogUtil.error("初始化插件失败：", e);
            }
        });
    }
}
