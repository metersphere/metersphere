package io.metersphere.sdk.service;

import io.metersphere.plugin.platform.api.AbstractPlatformPlugin;
import io.metersphere.plugin.sdk.api.MsPlugin;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.plugin.loader.PluginClassLoader;
import io.metersphere.sdk.plugin.loader.PluginManager;
import io.metersphere.sdk.plugin.storage.MsStorageStrategy;
import io.metersphere.sdk.plugin.storage.StorageStrategy;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.domain.PluginScript;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.PluginScriptMapper;
import jakarta.annotation.Resource;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginLoadService {

    private final PluginManager pluginManager = new PluginManager();

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private PluginScriptMapper pluginScriptMapper;

    /**
     * 上传插件到 minio
     */
    public void uploadPlugin(String id, MultipartFile file) {
        try {
            getStorageStrategy(id).store(file.getOriginalFilename(), file.getInputStream());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("文件上传异常", e);
        }
    }

    /**
     * @return 返回前端渲染需要的数据
     * 默认会返回 resources下的 script 下的 json 文件
     */
    public List<String> getFrontendScripts(String pluginId) {
        MsPlugin msPluginInstance = getMsPluginInstance(pluginId);
        String scriptDir = msPluginInstance.getScriptDir();
        StorageStrategy storageStrategy = pluginManager.getClassLoader(pluginId).getStorageStrategy();
        try {
            // 查询脚本文件名
            List<String> folderFileNames = storageStrategy.getFolderFileNames(scriptDir);
            // 获取脚本内容
            List<String> scripts = new ArrayList<>(folderFileNames.size());
            for (String folderFileName : folderFileNames) {
                InputStream in = storageStrategy.get(folderFileName);
                if (in == null) {
                    continue;
                }
                scripts.add(IOUtil.toString(storageStrategy.get(folderFileName)));
            }
            return scripts;
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("获取脚本异常", e);
        }
    }

    private static StorageStrategy getStorageStrategy(String id) {
        return new MsStorageStrategy(id);
    }

    public void loadPlugin(String id, MultipartFile file) {
        // 加载 jar
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            LogUtils.error(e);
            throw new MSException("获取文件输入流异常", e);
        }
        loadPlugin(id, inputStream, true);
    }

    public void loadPlugin(String pluginId, String fileName) {
        PluginClassLoader classLoader = pluginManager.getClassLoader(pluginId);
        if (classLoader != null) {
            return;
        }
        // 加载 jar
        InputStream inputStream;
        try {
            inputStream = classLoader.getStorageStrategy().get(fileName);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("下载文件异常", e);
        }
        loadPlugin(pluginId, inputStream, false);
    }

    public void loadPlugin(String id, InputStream inputStream, boolean isNeedUploadFile) {
        if (inputStream == null) {
            return;
        }
        loadPlugin(id, inputStream, new MsStorageStrategy(id), isNeedUploadFile);
    }

    /**
     * 加载插件
     *
     * @param id              插件ID
     * @param inputStream     输入流
     * @param storageStrategy 静态文件及jar包存储策略
     */
    public void loadPlugin(String id, InputStream inputStream, StorageStrategy storageStrategy, boolean isNeedUploadFile) {
        if (inputStream == null || pluginManager.getClassLoader(id) != null) {
            return;
        }
        // 加载 jar
        try {
            pluginManager.loadJar(id, inputStream,
                    storageStrategy == null ? getStorageStrategy(id) : storageStrategy, isNeedUploadFile);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("加载插件异常", e);
        }
    }

    /**
     * 项目启动时加载插件
     */
    public synchronized void loadPlugins() {
        List<Plugin> plugins = pluginMapper.selectByExample(new PluginExample());
        plugins.forEach(plugin -> {
            String id = plugin.getId();
            StorageStrategy storageStrategy = getStorageStrategy(id);
            try {
                InputStream inputStream = storageStrategy.get(plugin.getFileName());
                loadPlugin(id, inputStream, storageStrategy, false);
            } catch (Exception e) {
                LogUtils.error("初始化插件异常" + plugin.getFileName(), e);
            }
        });
    }

    /**
     * 卸载插件
     */
    public void unloadPlugin(String pluginId) {
        pluginManager.deletePlugin(pluginId);
    }

    /**
     * 删除插件
     */
    public void deletePlugin(String pluginId) {
        // 删除文件
        PluginClassLoader classLoader = pluginManager.getClassLoader(pluginId);
        try {
            if (classLoader != null) {
                classLoader.getStorageStrategy().delete();
            }
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("删除插件异常 ", e);
        }
        unloadPlugin(pluginId);
    }

    public MsPlugin getMsPluginInstance(String id) {
        return pluginManager.getImplInstance(id, MsPlugin.class);
    }

    public <T> T getImplInstance(String pluginId, Class<T> superClazz, Object param) {
        return pluginManager.getImplInstance(pluginId, superClazz, param);
    }

    public <T> T getImplInstance(String pluginId, Class<T> superClazz) {
        return pluginManager.getImplInstance(pluginId, superClazz);
    }

    public List<AbstractPlatformPlugin> getPlatformPluginInstanceList() {
        return getImplInstanceList(AbstractPlatformPlugin.class);
    }

    public AbstractPlatformPlugin getPlatformPluginInstance(String pluginId) {
        return getImplInstance(pluginId, AbstractPlatformPlugin.class);
    }

    public <T> List<T> getImplInstanceList(Class<T> clazz, Object... initArgs) {
        return pluginManager.getClassLoaderMap().keySet().stream()
                .map(pluginId -> pluginManager.getImplInstance(pluginId, clazz, initArgs)
        ).collect(Collectors.toList());
    }

    public boolean hasPluginKey(String currentPluginId, String pluginKey) {
        for (String pluginId : pluginManager.getClassLoaderMap().keySet()) {
            MsPlugin msPlugin = getMsPluginInstance(pluginId);
            if (!StringUtils.equals(currentPluginId, pluginId) && StringUtils.equals(msPlugin.getKey(), pluginKey)) {
                return true;
            }
        }
        return false;
    }

    public InputStream getResourceAsStream(String pluginId, String name) {
        return pluginManager.getClassLoaderMap().get(pluginId).getResourceAsStream(name);
    }

    public Map getPluginScriptConfig(String pluginId, String scriptId) {
        PluginScript pluginScript = pluginScriptMapper.selectByPrimaryKey(pluginId, scriptId);
        return JSON.parseMap(new String(pluginScript.getScript()));
    }

    public Object getPluginScriptContent(String pluginId, String scriptId) {
        return getPluginScriptConfig(pluginId, scriptId).get("script");
    }
}
