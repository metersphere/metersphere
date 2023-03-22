package io.metersphere.service;

import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.metadata.service.FileManagerService;
import io.metersphere.metadata.vo.FileRequest;
import io.metersphere.request.PluginDTO;
import io.metersphere.request.PluginRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private PlatformPluginService platformPluginService;
    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private FileManagerService fileManagerService;

    public void addPlugin(PluginWithBLOBs plugin) {
        if (StringUtils.isBlank(plugin.getId())) {
            plugin.setId(UUID.randomUUID().toString());
        }
        plugin.setCreateTime(System.currentTimeMillis());
        plugin.setUpdateTime(System.currentTimeMillis());
        pluginMapper.insert(plugin);
    }

    public List<PluginDTO> list(String name) {
        PluginExample example = new PluginExample();
        if (StringUtils.isNotBlank(name)) {
            name = "%" + name + "%";
            example.createCriteria().andNameLike(name);
        }
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        Map<String, Boolean> pluginMap = new HashMap<>();
        List<PluginDTO> lists = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(plugins)) {
            // 校验插件是否是企业版
            plugins.forEach(item -> {
                PluginDTO dto = new PluginDTO();
                BeanUtils.copyBean(dto, item);
                if (!StringUtils.equals(PluginScenario.platform.name(), item.getScenario())) {
                    // api 插件调用
                    if (!pluginMap.containsKey(item.getPluginId())) {
                        dto.setLicense(apiPluginService.isXpack(item));
                    } else {
                        dto.setLicense(pluginMap.get(item.getPluginId()));
                    }
                    pluginMap.put(item.getPluginId(), dto.getLicense());
                } else {
                    // 平台插件加载时已经保存
                    dto.setLicense(item.getXpack());
                }
                lists.add(dto);
            });
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

    public void delete(String scenario, String id) {
        if (StringUtils.equalsIgnoreCase(scenario, PluginScenario.platform.name())) {
            // 平台插件传的是 id
            platformPluginService.delete(id);
        } else {
            // 接口传的是 pluginId
            FileRequest request = getRequest(id);
            fileManagerService.delete(request);
            apiPluginService.delete(id);
        }
    }

    public Object customMethod(PluginRequest request) {
        try {
            Class<?> clazz = Class.forName(request.getEntry());
            Object instance = clazz.newInstance();
            return clazz.getDeclaredMethod("customMethod", String.class).invoke(instance, request.getRequest());
        } catch (Exception ex) {
            LogUtil.error("加载自定义方法失败：" + ex.getMessage());
        }
        return null;
    }

    public List<Plugin> list() {
        PluginExample example = new PluginExample();
        return pluginMapper.selectByExample(example);
    }

    public void addPlugin(MultipartFile file, String scenario) {
        checkPluginExist(file);
        try {
            if (StringUtils.equalsIgnoreCase(scenario, PluginScenario.platform.name())) {
                PluginWithBLOBs plugin = platformPluginService.addPlatformPlugin(file);
                platformPluginService.notifiedPlatformPluginAdd(plugin.getId());
            } else {
                List<PluginWithBLOBs> plugins = apiPluginService.addApiPlugin(file);
                plugins.forEach(this::addPlugin);
                // 存入MinIO
                if (CollectionUtils.isNotEmpty(plugins)) {
                    String pluginId = plugins.get(0).getPluginId();
                    FileRequest request = getRequest(pluginId);
                    fileManagerService.upload(file, request);
                } else {
                    MSException.throwException(Translator.get("plugin_parse_error"));
                }
            }
        } catch (Exception ex) {
            MSException.throwException(Translator.get("plugin_parse_error"));
        }

    }

    private FileRequest getRequest(String pluginId) {
        FileRequest request = new FileRequest();
        request.setProjectId(StringUtils.join(FileUtils.BODY_FILE_DIR, "/plugin", pluginId));
        request.setFileName(pluginId);
        request.setStorage(StorageConstants.MINIO.name());
        return request;
    }

    public void checkPluginExist(MultipartFile file) {
        String filename = file.getOriginalFilename();
        PluginExample example = new PluginExample();
        example.createCriteria().andSourceNameEqualTo(filename);
        List<Plugin> plugins = pluginMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(plugins)) {
            MSException.throwException("Plugin exist!");
        }
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

    public String getLogDetails(String id) {
        PluginExample example = new PluginExample();
        example.createCriteria().andPluginIdEqualTo(id);
        List<PluginWithBLOBs> plugins = pluginMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(plugins)) {
            Plugin plugin = plugins.get(0);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(plugin, SystemReference.pluginColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(plugin.getId()), null, plugin.getSourceName(), plugin.getCreateUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
