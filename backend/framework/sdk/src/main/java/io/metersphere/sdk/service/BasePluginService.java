package io.metersphere.sdk.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.mapper.PluginMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_ENABLE;
import static io.metersphere.sdk.controller.handler.result.CommonResultCode.PLUGIN_PERMISSION;

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
}
