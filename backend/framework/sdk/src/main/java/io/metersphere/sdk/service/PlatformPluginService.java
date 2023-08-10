package io.metersphere.sdk.service;

import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.plugin.platform.dto.PlatformRequest;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.*;
import io.metersphere.system.mapper.PluginMapper;
import io.metersphere.system.mapper.PluginOrganizationMapper;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_ENABLE;
import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_PERMISSION;

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
    private PluginOrganizationMapper pluginOrganizationMapper;

    /**
     * 获取平台实例
     *
     * @param pluginId
     * @param orgId
     * @param integrationConfig
     * @return
     */
    public Platform getPlatform(String pluginId, String orgId, String integrationConfig) {
        if (StringUtils.isNotBlank(orgId)) {
            // 服务集成的测试链接，不需要校验插件开启和状态
            Plugin plugin = pluginMapper.selectByPrimaryKey(pluginId);
            checkPluginEnable(plugin);
            checkPluginPermission(pluginId, orgId, plugin);
        }
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

    /**
     * 校验该组织是否能访问插件
     * @param pluginId
     * @param orgId
     * @param plugin
     */
    private void checkPluginPermission(String pluginId, String orgId, Plugin plugin) {
        if (plugin.getGlobal()) {
            return;
        }
        PluginOrganizationExample example = new PluginOrganizationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(orgId)
                .andPluginIdEqualTo(pluginId);
        List<PluginOrganization> pluginOrganizations = pluginOrganizationMapper.selectByExample(example);
        for (PluginOrganization pluginOrganization : pluginOrganizations) {
            if (StringUtils.equals(pluginOrganization.getOrganizationId(), orgId)) {
                return;
            }
        }
        throw new MSException(PLUGIN_PERMISSION);
    }

    /**
     * 校验插件是否启用
     * @param plugin
     */
    private static void checkPluginEnable(Plugin plugin) {
        if (BooleanUtils.isFalse(plugin.getEnable())) {
            throw new MSException(PLUGIN_ENABLE);
        }
    }

    private ServiceIntegration getServiceIntegrationByPluginId(String pluginId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andPluginIdEqualTo(pluginId);
        return serviceIntegrationMapper.selectByExampleWithBLOBs(example).get(0);
    }

    public List<Plugin> getPlatformPlugins() {
        PluginExample example = new PluginExample();
        example.createCriteria()
                .andScenarioEqualTo(PluginScenarioType.PLATFORM.name());
        return pluginMapper.selectByExample(example);
    }
}
