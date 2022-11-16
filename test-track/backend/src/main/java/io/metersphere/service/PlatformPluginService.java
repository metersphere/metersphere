package io.metersphere.service;

import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.loader.PlatformPluginManager;
import io.metersphere.utils.PluginManagerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private BasePluginService basePluginService;
    @Resource
    private BaseIntegrationService baseIntegrationService;

    private PlatformPluginManager pluginManager;

    /**
     * 查询所有平台插件并加载
     */
    public void loadPlatFormPlugins() {
        pluginManager = new PlatformPluginManager();
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        PluginManagerUtil.loadPlugins(pluginManager, plugins);
    }

    public void loadPlugin(String pluginId) {
        if (pluginManager.getClassLoader(pluginId) == null) {
            // 如果没有加载才加载
            InputStream pluginJar = basePluginService.getPluginJar(pluginId);
            PluginManagerUtil.loadPlugin(pluginId, pluginManager, pluginJar);
        }
    }

    /**
     * 卸载插件
     * @param pluginId
     */
    public void unloadPlugin(String pluginId) {
        pluginManager.deletePlugin(pluginId);
    }
}
