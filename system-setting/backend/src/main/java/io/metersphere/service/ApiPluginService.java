package io.metersphere.service;

import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.PluginResourceDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.plugin.core.api.UiScriptApi;
import io.metersphere.plugin.core.ui.PluginResource;
import io.metersphere.utils.CommonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiPluginService {

    @Resource
    private PluginMapper pluginMapper;

    public List<PluginWithBLOBs> addApiPlugin(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        String path = FileUtils.create(id, file);
        List<PluginWithBLOBs> addPlugins = new ArrayList<>();
        if (StringUtils.isNotEmpty(path)) {
            List<PluginResourceDTO> resources = this.getMethod(path, file.getOriginalFilename());
            if (CollectionUtils.isNotEmpty(resources)) {
                for (PluginResourceDTO resource : resources) {
                    PluginExample example = new PluginExample();
                    example.createCriteria().andPluginIdEqualTo(resource.getPluginId());
                    List<Plugin> plugins = pluginMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(plugins)) {
                        String delPath = plugins.get(0).getSourcePath();
                        FileUtils.deleteFile(delPath);
                        pluginMapper.deleteByExample(example);
                    }
                    this.create(resource, path, file.getOriginalFilename(), addPlugins);
                }
            }
        }
        return addPlugins;
    }

    private boolean loadJar(String jarPath) {
        validatePluginType(jarPath);
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            try {
                File file = new File(jarPath);
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

    /**
     * 校验是否是平台插件,避免被系统类加载器加载后,无法卸载,影响功能
     * @param jarPath
     */
    private void validatePluginType(String jarPath) {
        try {
            JarFile jar = new JarFile(jarPath);
            JarEntry entry = jar.getJarEntry("json/frontend.json");
            if (entry != null) {
                MSException.throwException(Translator.get("plugin_type_error"));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private List<PluginResourceDTO> getMethod(String path, String fileName) {
        List<PluginResourceDTO> resources = new LinkedList<>();
        try {
            this.loadJar(path);
            String jarPath[] = new String[]{path};
            List<String> classes = ClassFinder.findClassesThatExtend(jarPath, new Class[]{UiScriptApi.class}, true);
            for (String clazzName : classes) {
                UiScriptApi uiScriptApi = Class.forName(clazzName)
                        .asSubclass(UiScriptApi.class)
                        .getDeclaredConstructor().newInstance();
                PluginResource pluginObj = uiScriptApi.init();
                if (pluginObj != null) {
                    PluginResourceDTO pluginResourceDTO = new PluginResourceDTO();
                    BeanUtils.copyBean(pluginResourceDTO, pluginObj);
                    pluginResourceDTO.setEntry(clazzName);
                    resources.add(pluginResourceDTO);
                }
            }
        } catch (Exception e) {
            this.init(fileName, resources);
        }
        return resources;
    }

    private void init(String fileName, List<PluginResourceDTO> resources) {
        List<Class<?>> classes = CommonUtil.getSubClass(fileName);
        try {
            for (Class<?> aClass : classes) {
                Object instance = aClass.newInstance();
                Object pluginObj = aClass.getDeclaredMethod("init").invoke(instance);
                if (pluginObj != null) {
                    PluginResourceDTO pluginResourceDTO = new PluginResourceDTO();
                    BeanUtils.copyBean(pluginResourceDTO, (PluginResource) pluginObj);
                    pluginResourceDTO.setEntry(aClass.getName());
                    resources.add(pluginResourceDTO);
                }
            }
        } catch (Exception e) {
            LogUtil.error("初始化脚本异常：" + e.getMessage());
            MSException.throwException("调用插件初始化脚本失败");
        }
    }

    private void create(PluginResourceDTO resource, String path, String name, List<PluginWithBLOBs> addPlugins) {
        resource.getUiScripts().forEach(item -> {
            PluginWithBLOBs plugin = new PluginWithBLOBs();
            plugin.setName(item.getName());
            plugin.setPluginId(resource.getPluginId());
            plugin.setScriptId(item.getId());
            plugin.setSourcePath(path);
            plugin.setFormOption(item.getFormOption());
            plugin.setFormScript(item.getFormScript());
            plugin.setClazzName(item.getClazzName());
            plugin.setSourceName(name);
            plugin.setJmeterClazz(item.getJmeterClazz());
            plugin.setExecEntry(resource.getEntry());
            plugin.setCreateUserId(SessionUtils.getUserId());
            plugin.setScenario(PluginScenario.api.name());
            addPlugins.add(plugin);
        });
    }


    public boolean isXpack(Plugin item) {
        try {
            Class<?> clazz = Class.forName(item.getExecEntry());
            Object instance = clazz.newInstance();
            return isXpack(Class.forName(item.getExecEntry()), instance);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return false;
    }

    private boolean isXpack(Class<?> aClass, Object instance) {
        try {
            Object verify = aClass.getDeclaredMethod("xpack").invoke(instance);
            return (Boolean) verify;
        } catch (Exception e) {
            return false;
        }
    }

    public void delete(String id) {
        //通过pluginId判断是否还有其他脚本，无则清理加载的jar包
        PluginExample example = new PluginExample();
        example.createCriteria().andPluginIdEqualTo(id);
        List<Plugin> list = pluginMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            FileUtils.deleteFile(list.get(0).getSourcePath());
            pluginMapper.deleteByExample(example);
        }
    }
}
