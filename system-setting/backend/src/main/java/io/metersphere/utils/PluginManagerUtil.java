package io.metersphere.utils;

import im.metersphere.loader.PluginManager;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.service.plugin.MinioStorageStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PluginManagerUtil {

    public static void loadPlugin(String id, PluginManager pluginManager, MultipartFile file) {
        if (pluginManager == null) {
            pluginManager = new PluginManager();
        }

        MinioStorageStrategy minioStorageStrategy = new MinioStorageStrategy(id);

        try {
            // 上传到 sso
            minioStorageStrategy.store(file.getOriginalFilename(), file.getInputStream());
        } catch (IOException e) {
            LogUtil.error("上传 jar 包失败: ", e);
            MSException.throwException("上传 jar 包失败: " + e.getMessage());
        }

        // 加载 jar
        try {
            pluginManager.loadJar(id, file.getInputStream(), minioStorageStrategy);
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
