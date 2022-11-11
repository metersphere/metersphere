package io.metersphere.service;

import io.metersphere.base.domain.Plugin;
import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.request.PluginDTO;
import io.metersphere.request.PluginRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private ApiPluginService apiPluginService;

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
                if (StringUtils.equals(PluginScenario.api.name(), item.getScenario())) {
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
        if (StringUtils.equalsIgnoreCase(scenario, PluginScenario.platform.name())) {
            PluginWithBLOBs plugin = platformPluginService.addPlatformPlugin(file);
            addPlugin(plugin);
        } else {
            List<PluginWithBLOBs> plugins = apiPluginService.addApiPlugin(file);
            plugins.forEach(this::addPlugin);
        }
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
}
