package io.metersphere.sdk.service;

import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.plugin.platform.dto.PlatformRequest;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.ServiceIntegrationExample;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
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
        return pluginLoadService.getImplInstance(Platform.class, pluginId, pluginRequest);
    }

    public Platform getPlatform(String pluginId, String orgId) {
        ServiceIntegration serviceIntegration = getServiceIntegrationByPluginId(pluginId);
        return getPlatform(pluginId, orgId, new String(serviceIntegration.getConfiguration()));
    }

    private ServiceIntegration getServiceIntegrationByPluginId(String pluginId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        return serviceIntegrationMapper.selectByExampleWithBLOBs(example).get(0);
    }

    public List<Plugin> getOrgEnabledPlatformPlugins(String orgId) {
        return basePluginService.getOrgEnabledPlugins(orgId, PluginScenarioType.PLATFORM);
    }
}
