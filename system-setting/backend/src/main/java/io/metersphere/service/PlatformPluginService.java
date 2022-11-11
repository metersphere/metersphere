package io.metersphere.service;

import im.metersphere.loader.PluginManager;
import io.metersphere.api.PluginMetaInfo;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.utils.PluginManagerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private BasePluginService basePluginService;
    @Resource
    private PluginMapper pluginMapper;

    private PluginManager pluginManager;

    public PluginWithBLOBs addPlatformPlugin(MultipartFile file) {
        if (pluginManager != null) {
            pluginManager = new PluginManager();
        }
        String id = UUID.randomUUID().toString();

        PluginManagerUtil.loadPlugin(id, pluginManager, file);
        PluginMetaInfo pluginMetaInfo = pluginManager.getImplInstance(id, PluginMetaInfo.class);

        Map map = JSON.parseMap(pluginMetaInfo.getFrontendMetaData());
        map.put("id", id);
        map.put("key", pluginMetaInfo.getKey());

        PluginWithBLOBs plugin = new PluginWithBLOBs();
        plugin.setId(id);
        plugin.setName(file.getOriginalFilename());
        plugin.setPluginId(pluginMetaInfo.getKey() + "-" + pluginMetaInfo.getVersion());
        plugin.setScriptId(plugin.getPluginId());
        plugin.setSourcePath("");
//      plugin.setFormOption(item.getFormOption());
        plugin.setFormScript(JSON.toJSONString(map));
        plugin.setClazzName("");
        plugin.setSourceName(file.getOriginalFilename());
        plugin.setJmeterClazz("");
        plugin.setExecEntry("");
        plugin.setCreateUserId(SessionUtils.getUserId());
        plugin.setXpack(pluginMetaInfo.isXpack());
        plugin.setScenario(PluginScenario.platform.name());
        return plugin;
    }

    /**
     * 查询所有平台插件并加载
     */
    public void loadPlatFormPlugins() {
        pluginManager = new PluginManager();
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        PluginManagerUtil.loadPlugins(pluginManager, plugins);
    }

    public void getPluginResource(String pluginId, String name, HttpServletResponse response) {
        InputStream inputStream = pluginManager.getClassLoader(pluginId)
                .getResourceAsStream(name);
        getImage(inputStream, response);
    }

    public Object getIntegrationInfo() {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        List<Map> configs = new ArrayList<>();
        plugins.forEach(item ->{
            Map metaData = JSON.parseMap(item.getFormScript());
            Map serviceIntegration = (Map) metaData.get("serviceIntegration");
            serviceIntegration.put("id", metaData.get("id"));
            serviceIntegration.put("key", metaData.get("key"));
            configs.add(serviceIntegration);
        });
        return configs;
    }

    public void getImage(InputStream in, HttpServletResponse response) {
        response.setContentType("image/png");
        try(OutputStream out = response.getOutputStream()) {
            out.write(in.readAllBytes());
            out.flush();
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                LogUtil.error(e);
            }
        }
    }

    public void delete(String id) {
        pluginMapper.deleteByPrimaryKey(id);
        try {
            pluginManager.getClassLoader(id).getStorageStrategy().delete();
            pluginManager.deletePlugin(id);
        } catch (IOException e) {
            LogUtil.error(e);
        }
    }
}
