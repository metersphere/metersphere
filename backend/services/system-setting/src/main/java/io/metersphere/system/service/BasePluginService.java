package io.metersphere.system.service;

import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.PluginExample;
import io.metersphere.system.domain.PluginOrganization;
import io.metersphere.system.mapper.PluginMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.handler.result.CommonResultCode.PLUGIN_ENABLE;
import static io.metersphere.system.controller.handler.result.CommonResultCode.PLUGIN_PERMISSION;

/**
 * @author jianxing
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BasePluginService {
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private BasePluginOrganizationService basePluginOrganizationService;

    public void checkPluginEnableAndPermission(String pluginId, String orgId) {
        Plugin plugin = pluginMapper.selectByPrimaryKey(pluginId);
        if (!plugin.getEnable()) {
            throw new MSException(PLUGIN_ENABLE);
        }
        if (!plugin.getGlobal() && CollectionUtils.isEmpty(basePluginOrganizationService.getByPluginIdAndOrgId(pluginId, orgId))) {
            throw new MSException(PLUGIN_PERMISSION);
        }
    }

    public Plugin get(String pluginId) {
        return pluginMapper.selectByPrimaryKey(pluginId);
    }

    public List<Plugin> getOrgEnabledPlugins(String orgId, PluginScenarioType pluginScenarioType) {
        List<Plugin> plugins = getEnabledPlugins(pluginScenarioType);
        List<String> unGlobalIds = plugins.stream().filter(i -> !i.getGlobal()).map(Plugin::getId).toList();
        // 如果没有非全局，直接返回全局插件
        if (CollectionUtils.isEmpty(unGlobalIds)) {
            return plugins;
        }
        // 查询当前组织下的插件列表
        List<PluginOrganization> pluginOrganizations = basePluginOrganizationService.getByPluginIds(unGlobalIds);
        Set<String> orgPluginIdSet = pluginOrganizations.stream()
                .filter(i -> StringUtils.equals(i.getOrganizationId(), orgId))
                .map(PluginOrganization::getPluginId)
                .collect(Collectors.toSet());
        // 返回全局插件和当前组织下的插件
        return plugins.stream().filter(i -> i.getGlobal() || orgPluginIdSet.contains(i.getId())).collect(Collectors.toList());
    }

    public List<Plugin> getEnabledPlugins(PluginScenarioType PluginScenarioType) {
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andEnableEqualTo(true)
                .andScenarioEqualTo(PluginScenarioType.name());
        return pluginMapper.selectByExample(example);
    }
}
