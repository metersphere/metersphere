package io.metersphere.system.service;

import io.metersphere.plugin.api.spi.AbstractProtocolPlugin;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.system.domain.Plugin;
import jakarta.annotation.Resource;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private BasePluginService basePluginService;

    /**
     * 获取协议插件的的协议列表
     * @param orgId
     * @return
     */
    public List<String> getProtocols(String orgId) {
        // 查询组织下有权限的插件
        Set<String> pluginIds = basePluginService.getOrgEnabledPlugins(orgId, PluginScenarioType.API_PROTOCOL)
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());

        List<PluginWrapper> plugins = pluginLoadService.getMsPluginManager().getPlugins();
        return plugins.stream()
                .filter(plugin -> pluginIds.contains(plugin.getPluginId()) && plugin.getPlugin() instanceof AbstractProtocolPlugin)
                .map(plugin -> ((AbstractProtocolPlugin) plugin.getPlugin()).getProtocol())
                .collect(Collectors.toList());
    }
}
