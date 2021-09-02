package io.metersphere.service;

import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.PluginRequest;
import io.metersphere.controller.request.PluginResourceDTO;
import io.metersphere.plugin.core.ui.PluginResource;
import io.metersphere.service.utils.CommonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {
    @Resource
    private PluginMapper pluginMapper;

    public String editPlugin(MultipartFile file) {
        String id = UUID.randomUUID().toString();
        String path = FileUtils.create(id, file);
        if (StringUtils.isNotEmpty(path)) {
            List<PluginResourceDTO> resources = this.getMethod(path, file.getOriginalFilename());
            if (CollectionUtils.isNotEmpty(resources)) {
                for (PluginResourceDTO resource : resources) {
                    PluginExample example = new PluginExample();
                    example.createCriteria().andPluginIdEqualTo(resource.getPluginId());
                    List<Plugin> plugins = pluginMapper.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(plugins)) {
                        String delPath = plugins.get(0).getSourcePath();
                        // this.closeJar(delPath);
                        FileUtils.deleteFile(delPath);
                        pluginMapper.deleteByExample(example);
                    }
                    this.create(resource, path, file.getOriginalFilename());
                }
            }
        }
        return null;
    }

    private void create(PluginResourceDTO resource, String path, String name) {
        resource.getUiScripts().forEach(item -> {
            PluginWithBLOBs plugin = new PluginWithBLOBs();
            plugin.setId(UUID.randomUUID().toString());
            plugin.setCreateTime(System.currentTimeMillis());
            plugin.setUpdateTime(System.currentTimeMillis());
            plugin.setName(item.getName());
            plugin.setPluginId(resource.getPluginId());
            plugin.setSourcePath(path);
            plugin.setFormOption(item.getFormOption());
            plugin.setFormScript(item.getFormScript());
            plugin.setClazzName(item.getClazzName());
            plugin.setSourceName(name);
            plugin.setExecEntry(resource.getEntry());
            plugin.setCreateUserId(SessionUtils.getUserId());
            pluginMapper.insert(plugin);
        });
    }

    private List<PluginResourceDTO> getMethod(String path, String fileName) {
        List<PluginResourceDTO> resources = new LinkedList<>();
        try {
            this.loadJar(path);
            List<Class<?>> classes = CommonUtil.getSubClass(fileName);
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
        }
        return resources;
    }

    private void closeJar(String jarPath) {
        File jarFile = new File(jarPath);
        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }
        // 获取方法的访问权限以便写回
        try {
            method.setAccessible(true);
            // 获取系统类加载器
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL url = jarFile.toURI().toURL();
            method.invoke(classLoader, url);
            classLoader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void loadJar(String jarPath) {
        File jarFile = new File(jarPath);
        // 从URLClassLoader类中获取类所在文件夹的方法，jar也可以认为是一个文件夹
        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }
        // 获取方法的访问权限以便写回
        try {
            method.setAccessible(true);
            // 获取系统类加载器
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL url = jarFile.toURI().toURL();
            method.invoke(classLoader, url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void loadPlugins() {
        PluginExample example = new PluginExample();
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(plugins)) {
            plugins.forEach(item -> {
                this.loadJar(item.getSourcePath());
            });
        }
    }

    public List<Plugin> list() {
        PluginExample example = new PluginExample();
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        return plugins;
    }

    public Plugin get(String pluginId) {
        PluginExample example = new PluginExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        List<PluginWithBLOBs> plugins = pluginMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(plugins)) {
            return plugins.get(0);
        }
        return null;
    }

    public String delete(String id) {
        Plugin plugin = pluginMapper.selectByPrimaryKey(id);
        if (plugin != null) {
            //通过pluginId判断是否还有其他脚本，无则清理加载的jar包
            PluginExample example = new PluginExample();
            example.createCriteria().andPluginIdEqualTo(plugin.getPluginId());
            List<Plugin> plugins = pluginMapper.selectByExample(example);
            if (plugins.size() == 1) {
                // this.closeJar(plugin.getSourcePath());
                FileUtils.deleteFile(plugin.getSourcePath());
            }
            pluginMapper.deleteByPrimaryKey(id);
            return "success";
        }
        return "error";
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
}
