package io.metersphere.system.service;


import io.metersphere.plugin.sdk.api.MsPlugin;
import io.metersphere.sdk.constants.KafkaPluginTopicType;
import io.metersphere.sdk.constants.KafkaTopicConstants;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.dto.PluginDTO;
import io.metersphere.system.mapper.ExtPluginMapper;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.request.PluginUpdateRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_EXIST;
import static io.metersphere.system.controller.result.SystemResultCode.PLUGIN_TYPE_EXIST;

/**
 * @author jianxing
 * @date : 2023-7-13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PluginService {

    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private ExtPluginMapper extPluginMapper;
    @Resource
    private PluginScriptService pluginScriptService;
    @Resource
    private PluginOrganizationService pluginOrganizationService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public List<PluginDTO> list() {
        List<PluginDTO> plugins = extPluginMapper.getPlugins();
        List<String> pluginIds = plugins.stream().map(Plugin::getId).toList();
        Map<String, List<OptionDTO>> scripteMap = pluginScriptService.getScripteMap(pluginIds);
        Map<String, List<OptionDTO>> orgMap = pluginOrganizationService.getOrgMap(pluginIds);
        plugins.forEach(plugin -> {
            plugin.setPluginForms(scripteMap.get(plugin.getId()));
            plugin.setOrganizations(orgMap.get(plugin.getId()));
        });
        return plugins;
    }

    public Plugin get(String id) {
        return pluginMapper.selectByPrimaryKey(id);
    }

    public Plugin add(PluginUpdateRequest request, MultipartFile file) {
        String id = UUID.randomUUID().toString();
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setId(id);
        plugin.setFileName(file.getOriginalFilename());
        plugin.setCreateTime(System.currentTimeMillis());
        plugin.setUpdateTime(System.currentTimeMillis());
        // 如果没有，默认设置为 true
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));
        request.setEnable(!BooleanUtils.isFalse(request.getEnable()));

        // 校验重名
        checkPluginAddExist(plugin);

        try {
            // 加载插件
            pluginLoadService.loadPlugin(id, file);
            // 上传插件
            pluginLoadService.uploadPlugin(id, file);
            // 获取插件前端配置脚本
            List<String> frontendScript = pluginLoadService.getFrontendScripts(id);

            MsPlugin msPlugin = pluginLoadService.getMsPluginInstance(id);
            plugin.setScenario(msPlugin.getType());
            plugin.setXpack(msPlugin.isXpack());
            plugin.setPluginId(msPlugin.getPluginId());

            // 校验插件类型是否重复
            checkPluginKeyExist(id, msPlugin.getKey());

            // 保存插件脚本
            pluginScriptService.add(id, frontendScript);

            // 保存插件和组织的关联关系
            if (!request.getGlobal()) {
                pluginOrganizationService.add(id, request.getOrganizationIds());
            }

            pluginMapper.insert(plugin);

            // 通知其他节点加载插件
            notifiedPluginAdd(id, plugin.getFileName());
        } catch (Exception e) {
            // 删除插件
            pluginLoadService.deletePlugin(id);
            throw e;
        }
        return plugin;
    }

    private void checkPluginKeyExist(String pluginId, String pluginKey) {
        if (pluginLoadService.hasPluginKey(pluginId, pluginKey)) {
            throw new MSException(PLUGIN_TYPE_EXIST);
        }
    }

    private void checkPluginAddExist(Plugin plugin) {
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andNameEqualTo(plugin.getName());
        PluginExample.Criteria criteria = example.createCriteria()
                .andFileNameEqualTo(plugin.getFileName());
        example.or(criteria);
        if (CollectionUtils.isNotEmpty(pluginMapper.selectByExample(example))) {
            throw new MSException(PLUGIN_EXIST);
        }
    }

    /**
     *  通知其他节点加载插件
     *  这里需要传一下 fileName，事务未提交，查询不到文件名
     * @param pluginId
     * @param fileName
     */
    public void notifiedPluginAdd(String pluginId, String fileName) {
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PLUGIN, String.format("%s:%s:%s", KafkaPluginTopicType.ADD, pluginId, fileName));
    }

    /**
     *  通知其他节点卸载插件
     * @param pluginId
     */
    public void notifiedPluginDelete(String pluginId) {
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PLUGIN, String.format("%s:%s", KafkaPluginTopicType.DELETE, pluginId));
    }

    public Plugin update(PluginUpdateRequest request) {
        request.setCreateUser(null);
        Plugin plugin = new Plugin();
        BeanUtils.copyBean(plugin, request);
        plugin.setCreateTime(null);
        plugin.setUpdateTime(null);
        // 校验重名
        checkPluginUpdateExist(plugin);
        pluginMapper.updateByPrimaryKeySelective(plugin);
        if (BooleanUtils.isTrue(request.getGlobal())) {
            // 全局插件，删除和组织的关联关系
            request.setOrganizationIds(new ArrayList<>(0));
        }
        pluginOrganizationService.update(plugin.getId(), request.getOrganizationIds());
        return plugin;
    }

    private void checkPluginUpdateExist(Plugin plugin) {
        if (StringUtils.isBlank(plugin.getName())) {
            return;
        }
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andIdNotEqualTo(plugin.getId())
                .andNameEqualTo(plugin.getName());
        if (CollectionUtils.isNotEmpty(pluginMapper.selectByExample(example))) {
            throw new MSException(PLUGIN_EXIST);
        }
    }

    public void delete(String id) {
        pluginMapper.deleteByPrimaryKey(id);
        // 删除插件脚本
        pluginScriptService.deleteByPluginId(id);
        // 删除和组织的关联关系
        pluginOrganizationService.deleteByPluginId(id);
        // 删除和卸载插件
        pluginLoadService.deletePlugin(id);
        notifiedPluginDelete(id);
    }

    public String getScript(String pluginId, String scriptId) {
        return pluginScriptService.get(pluginId, scriptId);
    }
}