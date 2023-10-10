package io.metersphere.system.service;

import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.system.controller.handler.result.CommonResultCode;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.file.FileCenter;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.system.plugin.MsPluginManager;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.MsFileUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.domain.PluginScript;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.PluginScriptMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginLoadService {

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private PluginScriptMapper pluginScriptMapper;
    private MsPluginManager msPluginManager = new MsPluginManager();

    /**
     * 从文件系统中加载jar
     *
     * @param fileName
     * @return
     */
    public String loadPlugin(String fileName) {
        return msPluginManager.loadPlugin(Paths.get(MsFileUtils.PLUGIN_DIR + "/" + fileName));
    }


    /**
     * 从默认的对象存储下载插件到本地，再加载
     * @param fileName
     * @return
     * @throws Exception
     */
    public void loadPluginFromRepository(String fileName) {
        String filePath = MsFileUtils.PLUGIN_DIR + "/" + fileName;
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                InputStream fileAsStream = FileCenter.getDefaultRepository().getFileAsStream(getFileRequest(fileName));
                FileUtils.copyInputStreamToFile(fileAsStream, file);
            }
            msPluginManager.loadPlugin(Paths.get(filePath));
        } catch (Exception e) {
            LogUtils.error("从对象存储加载插件异常", e);
        }
    }

    /**
     * 将插件上传到本地文件系统中
     *
     * @param file
     * @return
     */
    public String uploadPlugin2Local(MultipartFile file) {
        try {
            return FileCenter.getRepository(StorageType.LOCAL).saveFile(file, getFileRequest(file.getOriginalFilename()));
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("文件上传异常", e);
        }
    }

    /**
     * 将文件上传到默认的对象存储中
     *
     * @param file
     */
    public void uploadPlugin2Repository(MultipartFile file) {
        try {
            FileCenter.getDefaultRepository().saveFile(file, getFileRequest(file.getOriginalFilename()));
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("文件上传异常", e);
        }
    }

    private FileRequest getFileRequest(String name) {
        FileRequest request = new FileRequest();
        request.setProjectId(MsFileUtils.PLUGIN_DIR_NAME);
        request.setFileName(name);
        return request;
    }

    /**
     * @return 返回前端渲染需要的数据
     * 默认会返回 resources下的 script 下的 json 文件
     */
    public List<String> getFrontendScripts(String pluginId) {
        MsPlugin msPluginInstance = (MsPlugin) msPluginManager.getPlugin(pluginId).getPlugin();
        String scriptDir = msPluginInstance.getScriptDir();
        List<String> scripts = new ArrayList<>(10);
        String jarPath = msPluginManager.getPlugin(pluginId).getPluginPath().toString();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                //获取文件路径
                String innerPath = jarEntry.getName();
                if (innerPath.startsWith(scriptDir) && !jarEntry.isDirectory()) {
                    //获取到文件流
                    try (InputStream inputStream = msPluginManager.getPluginClassLoader(pluginId).getResourceAsStream(innerPath)) {
                        if (inputStream != null) {
                            scripts.add(IOUtil.toString(inputStream));
                        }
                    }
                }
            }
            return scripts;
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException("获取脚本异常", e);
        }
    }

    /**
     * 项目启动时加载插件
     */
    public synchronized void loadPlugins() {
        List<Plugin> plugins = pluginMapper.selectByExample(new PluginExample());
        plugins.forEach(plugin -> {
            String fileName = plugin.getFileName();
            try {
                loadPlugin(fileName);
                msPluginManager.startPlugin(plugin.getId());
            } catch (Throwable e) {
                LogUtils.error("初始化插件异常" + plugin.getFileName(), e);
            }
        });
    }

    /**
     * 卸载插件
     */
    public void unloadPlugin(String pluginId) {
        if (msPluginManager.getPlugin(pluginId) != null) {
            msPluginManager.deletePlugin(pluginId);
        }
    }

    public boolean hasPlugin(String pluginId) {
        return msPluginManager.getPlugin(pluginId) != null;
    }

    /**
     * 删除插件
     */
    public void deletePluginFile(String fileName) {
        FileRequest fileRequest = getFileRequest(fileName);
        try {
            FileCenter.getRepository(StorageType.LOCAL).delete(fileRequest);
            FileCenter.getDefaultRepository().delete(fileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    /**
     * 删除本地插件
     * @param fileName
     */
    public void deleteLocalPluginFile(String fileName) {
        FileRequest fileRequest = getFileRequest(fileName);
        try {
            FileCenter.getRepository(StorageType.LOCAL).delete(fileRequest);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    public InputStream getResourceAsStream(String pluginId, String name) {
        return msPluginManager.getPluginClassLoader(pluginId).getResourceAsStream(name);
    }

    public Map getPluginScriptConfig(String pluginId, String scriptId) {
        PluginScript pluginScript = pluginScriptMapper.selectByPrimaryKey(pluginId, scriptId);
        return JSON.parseMap(new String(pluginScript.getScript()));
    }

    public Object getPluginScriptContent(String pluginId, String scriptId) {
        return getPluginScriptConfig(pluginId, scriptId).get("script");
    }

    public PluginWrapper getPluginWrapper(String id) {
        return msPluginManager.getPlugin(id);
    }

    /**
     * 获取插件中的是实现类列表
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> List<T> getExtensions(Class<T> clazz) {
        return msPluginManager.getExtensions(clazz);
    }

    /**
     * 获取插件中的是实现类
     * @param clazz
     * @param pluginId
     * @return
     * @param <T>
     */
    public <T> Class<? extends T> getExtensionsClass(Class<T> clazz, String pluginId) {
        List<Class<? extends T>> classes = msPluginManager.getExtensionClasses(clazz, pluginId);
        return CollectionUtils.isEmpty(classes) ? null : classes.get(0);
    }

    public MsPluginManager getMsPluginManager() {
        return msPluginManager;
    }

    public <T> T getImplInstance(Class<T> extensionClazz, String pluginId, Object param) {
        try {
            Class<? extends T> clazz = getExtensionsClass(extensionClazz, pluginId);
            if (clazz == null) {
                throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE);
            }
            if (param == null) {
                return clazz.getConstructor().newInstance();
            } else {
                return clazz.getConstructor(param.getClass()).newInstance(param);
            }
        } catch (InvocationTargetException e) {
            LogUtils.error(e.getTargetException());
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getTargetException().getMessage());
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(CommonResultCode.PLUGIN_GET_INSTANCE, e.getMessage());
        }
    }

    public void handlePluginAddNotified(String pluginId, String fileName) {
        if (!hasPlugin(pluginId)) {
            loadPluginFromRepository(fileName);
        }
    }

    public void handlePluginDeleteNotified(String pluginId, String fileName) {
        if (hasPlugin(pluginId)) {
            unloadPlugin(pluginId);
            deleteLocalPluginFile(fileName);
        }
    }
}
