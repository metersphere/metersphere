package io.metersphere.service;

import io.metersphere.api.dto.plugin.PluginDTO;
import io.metersphere.api.dto.plugin.PluginRequest;
import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.config.MinioConfig;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.PluginConfigDTO;
import io.metersphere.dto.PluginInfoDTO;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private FileManagerService fileManagerService;

    private boolean isXpack(Class<?> aClass, Object instance) {
        try {
            Object verify = aClass.getDeclaredMethod("xpack").invoke(instance);
            return (Boolean) verify;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean loadJar(String jarPath, String pluginId) {
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try {
                File file = new File(jarPath);
                if (!file.exists()) {
                    // 从MinIO下载
                    if (!this.downPluginJar(jarPath, pluginId, jarPath)) {
                        return false;
                    }
                }
                if (!file.exists()) {
                    return false;
                }
                Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, file.toURI().toURL());
            } catch (NoSuchMethodException e) {
                Method method = classLoader.getClass()
                        .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
                method.setAccessible(true);
                method.invoke(classLoader, jarPath);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;
    }

    private boolean downPluginJar(String path, String pluginId, String jarPath) {
        FileRequest request = new FileRequest();
        request.setProjectId(StringUtils.join(FileUtils.BODY_FILE_DIR, "/plugin", pluginId));
        request.setFileName(pluginId);
        request.setStorage(StorageConstants.MINIO.name());
        byte[] bytes = fileManagerService.downloadFile(request);
        if (ArrayUtils.isNotEmpty(bytes)) {
            FileUtils.createFile(path, bytes);
        }
        return new File(jarPath).exists();
    }

    public void loadPlugins() {
        try {
            PluginExample example = new PluginExample();
            example.createCriteria().andScenarioNotEqualTo(PluginScenario.platform.name());
            List<Plugin> plugins = pluginMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(plugins)) {
                plugins = plugins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                        -> new TreeSet<>(Comparator.comparing(Plugin::getPluginId))), ArrayList::new));
                if (CollectionUtils.isNotEmpty(plugins)) {
                    plugins.forEach(item -> {
                        boolean isLoad = this.loadJar(item.getSourcePath(), item.getPluginId());
                        if (!isLoad) {
                            PluginExample pluginExample = new PluginExample();
                            pluginExample.createCriteria().andPluginIdEqualTo(item.getPluginId());
                            pluginMapper.deleteByExample(pluginExample);
                        }
                    });
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public List<PluginDTO> getPluginList() {
        List<PluginDTO> lists = new LinkedList<>();
        try {
            PluginExample example = new PluginExample();
            example.createCriteria().andScenarioNotEqualTo(PluginScenario.platform.name());
            List<Plugin> plugins = pluginMapper.selectByExample(example);
            Map<String, Boolean> pluginMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(plugins)) {
                // 校验插件是否是企业版
                plugins.forEach(item -> {
                    PluginDTO dto = new PluginDTO();
                    BeanUtils.copyBean(dto, item);
                    if (!pluginMap.containsKey(item.getPluginId())) {
                        try {
                            Class<?> clazz = Class.forName(item.getExecEntry());
                            Object instance = clazz.newInstance();
                            dto.setLicense(this.isXpack(Class.forName(item.getExecEntry()), instance));
                        } catch (Exception e) {
                            LogUtil.error(e.getMessage());
                        }
                    } else {
                        dto.setLicense(pluginMap.get(item.getPluginId()));
                    }
                    lists.add(dto);
                    pluginMap.put(item.getPluginId(), dto.getLicense());
                });
                return lists;
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return lists;
    }

    public Plugin get(String scriptId) {
        PluginExample example = new PluginExample();
        example.createCriteria().andScriptIdEqualTo(scriptId);
        List<PluginWithBLOBs> plugins = pluginMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(plugins)) {
            return plugins.get(0);
        }
        return null;
    }

    public String delete(String id) {
        //通过pluginId判断是否还有其他脚本，无则清理加载的jar包
        PluginExample example = new PluginExample();
        example.createCriteria().andPluginIdEqualTo(id);
        List<Plugin> list = pluginMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            FileUtils.deleteFile(list.get(0).getSourcePath());
            pluginMapper.deleteByExample(example);
        }
        return "success";
    }

    public Object customMethod(PluginRequest request) {
        try {
            Class<?> clazz = Class.forName(request.getEntry());
            Object instance = clazz.newInstance();
            Object pluginObj = clazz.getDeclaredMethod("customMethod", String.class).invoke(instance, request.getRequest());
            return pluginObj;
        } catch (Exception ex) {
            LogUtil.error("加载自定义方法失败：" + ex.getMessage());
        }
        return null;
    }

    public List<Plugin> list() {
        PluginExample example = new PluginExample();
        example.createCriteria().andScenarioNotEqualTo(PluginScenario.platform.name());
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        return plugins;
    }

    public PluginConfigDTO getPluginConfig() {
        PluginConfigDTO pluginConfigDTO = new PluginConfigDTO();
        List<Plugin> plugins = this.list();
        if (CollectionUtils.isNotEmpty(plugins)) {
            plugins = plugins.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                    new TreeSet<>(Comparator.comparing(Plugin::getPluginId))), ArrayList::new));
            List<PluginInfoDTO> plugin = plugins.stream().map(
                    item -> {
                        PluginInfoDTO pluginDTO = new PluginInfoDTO();
                        pluginDTO.setPluginId(item.getPluginId());
                        pluginDTO.setSourcePath(item.getSourcePath());
                        return pluginDTO;
                    }
            ).collect(Collectors.toList());
            pluginConfigDTO.setPluginDTOS(plugin);
        }
        pluginConfigDTO.setConfig(MinioConfig.getMinio());
        return pluginConfigDTO;
    }
}
