package io.metersphere.sdk.service;

import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.plugin.platform.dto.PlatformRequest;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.system.domain.*;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private BasePluginOrganizationService basePluginOrganizationService;
    @Resource
    private BasePluginService basePluginService;

    /**
     * 获取平台实例
     *
     * @param pluginId
     * @param orgId
     * @param integrationConfig
     * @return
     */
    public Platform getPlatform(String pluginId, String orgId, String integrationConfig) {
        basePluginService.checkPluginEnableAndPermission(pluginId, orgId);
        PlatformRequest pluginRequest = new PlatformRequest();
        pluginRequest.setIntegrationConfig(integrationConfig);
        pluginRequest.setOrganizationId(orgId);
        return pluginLoadService.getImplInstance(pluginId, Platform.class, pluginRequest);
    }

    public Platform getPlatform(String pluginId, String orgId) {
        // 这里会校验插件是否存在
        pluginLoadService.getMsPluginInstance(pluginId);
        ServiceIntegration serviceIntegration = getServiceIntegrationByPluginId(pluginId);
        return getPlatform(pluginId, orgId, new String(serviceIntegration.getConfiguration()));
    }

    private ServiceIntegration getServiceIntegrationByPluginId(String pluginId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        return serviceIntegrationMapper.selectByExampleWithBLOBs(example).get(0);
    }

    public List<Plugin> getEnabledPlatformPlugins() {
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andEnableEqualTo(true)
                .andScenarioEqualTo(PluginScenarioType.PLATFORM.name());
        return pluginMapper.selectByExample(example);
    }

    public List<Plugin> getOrgEnabledPlatformPlugins(String orgId) {
        List<Plugin> plugins = getEnabledPlatformPlugins();
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
}
