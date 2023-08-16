package io.metersphere.service;

import io.metersphere.base.domain.PluginExample;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.PluginMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.PlatformProjectOptionRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.platform.api.Platform;
import io.metersphere.platform.api.PluginMetaInfo;
import io.metersphere.platform.domain.GetOptionRequest;
import io.metersphere.platform.domain.PlatformRequest;
import io.metersphere.platform.domain.SelectOption;
import io.metersphere.platform.loader.PlatformPluginManager;
import io.metersphere.request.IntegrationRequest;
import io.metersphere.utils.PluginManagerUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private BasePluginService basePluginService;
    @Resource
    private PluginMapper pluginMapper;
    @Resource
    private BaseIntegrationService baseIntegrationService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    @Lazy
    private PluginService pluginService;

    private static final String PLUGIN_DOWNLOAD_URL = "https://github.com/metersphere/metersphere-platform-plugin";

    private PlatformPluginManager pluginManager;

    public synchronized PlatformPluginManager getPluginManager() {
        if (pluginManager == null) {
            pluginManager = new PlatformPluginManager();
        }
        return pluginManager;
    }

    /**
     * 新开一个事务，保证发送 kafka 消息是在事务提交之后
     * 因为接受消息需要判断数据库是否有数据
     * @param file
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public PluginWithBLOBs addPlatformPlugin(MultipartFile file) {
        String id = UUID.randomUUID().toString();

        PluginManagerUtil.loadPlugin(id, getPluginManager(), file);
        PluginMetaInfo pluginMetaInfo = getPluginManager().getImplInstance(id, PluginMetaInfo.class);

        PluginExample example = new PluginExample();
        example.createCriteria().andScriptIdEqualTo(pluginMetaInfo.getKey());
        if (pluginMapper.countByExample(example) > 0) {
            // 校验插件类型是否存在
            unload(id);
            MSException.throwException(pluginMetaInfo.getKey() + Translator.get("platform_plugin_exit"));
        }

        PluginManagerUtil.uploadPlugin(id, file);

        Map map = JSON.parseMap(pluginMetaInfo.getFrontendMetaData());
        map.put("id", id);
        map.put("key", pluginMetaInfo.getKey());

        PluginWithBLOBs plugin = new PluginWithBLOBs();
        plugin.setId(id);
        plugin.setName(file.getOriginalFilename());
        plugin.setPluginId(pluginMetaInfo.getKey() + "-" + pluginMetaInfo.getVersion());
        plugin.setScriptId(pluginMetaInfo.getKey());
        plugin.setSourcePath("");
        plugin.setFormScript(JSON.toJSONString(map));
        plugin.setClazzName("");
        plugin.setSourceName(file.getOriginalFilename());
        plugin.setJmeterClazz("");
        plugin.setExecEntry("");
        plugin.setCreateUserId(SessionUtils.getUserId());
        plugin.setXpack(pluginMetaInfo.isXpack());
        plugin.setScenario(PluginScenario.platform.name());

        pluginService.addPlugin(plugin);
        return plugin;
    }

    public void notifiedPlatformPluginAdd(String pluginId) {
        // 初始化项目默认节点
        kafkaTemplate.send(KafkaTopicConstants.PLATFORM_PLUGIN, "ADD:" + pluginId);
    }

    /**
     * 查询所有平台插件并加载
     */
    public void loadPlatFormPlugins() {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        PluginManagerUtil.loadPlugins(getPluginManager(), plugins);
    }

    public void getPluginResource(String pluginId, String name, HttpServletResponse response) {
        InputStream inputStream = getPluginManager().getClassLoader(pluginId)
                .getResourceAsStream(name);
        getImage(inputStream, response);
    }

    public Object getIntegrationInfo() {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        List<Map> configs = new ArrayList<>();
        plugins.forEach(item -> configs.add(getFrontendMetaDataConfig(item, "serviceIntegration")));
        return configs;
    }

    public Map getProjectInfo(String key) {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        for (PluginWithBLOBs plugin : plugins) {
            if (StringUtils.equals(plugin.getScriptId(), key)) {
                return getFrontendMetaDataConfig(plugin, "projectConfig");
            }
        }
        return null;
    }

    public List getAccountInfoList() {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        List<Map> configs = new ArrayList<>();
        plugins.forEach(item -> configs.add(getFrontendMetaDataConfig(item, "accountConfig")));

        // 过滤掉服务集成中没有的
        List<ServiceIntegration> integrations = baseIntegrationService.getAll(SessionUtils.getCurrentWorkspaceId());
        return configs.stream()
                .filter(config ->
                        integrations.stream()
                                .filter(integration -> StringUtils.equals(integration.getPlatform(), config.get("key").toString()))
                                .collect(Collectors.toList()).size() > 0
                ).collect(Collectors.toList());
    }

    public List<SelectOption> getProjectOption(PlatformProjectOptionRequest request) {
        Platform platform = getPlatform(request.getPlatform(), request.getWorkspaceId());
        GetOptionRequest getOptionRequest = new GetOptionRequest();
        getOptionRequest.setOptionMethod(request.getOptionMethod());
        getOptionRequest.setProjectConfig(request.getProjectConfig());
        try {
            return platform.getProjectOptions(getOptionRequest);
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    public Platform getPlatform(String platformKey, String workspaceId) {
        IntegrationRequest integrationRequest = new IntegrationRequest();
        integrationRequest.setPlatform(platformKey);
        integrationRequest.setWorkspaceId(StringUtils.isBlank(workspaceId) ? SessionUtils.getCurrentWorkspaceId() : workspaceId);
        ServiceIntegration serviceIntegration = baseIntegrationService.get(integrationRequest);

        PlatformRequest pluginRequest = new PlatformRequest();
        pluginRequest.setIntegrationConfig(serviceIntegration.getConfiguration());
        pluginRequest.setUserPlatformInfo(getUserPlatformInfo(workspaceId));
        Platform platform = getPluginManager().getPlatformByKey(platformKey, pluginRequest);
        if (platform == null) {
            MSException.throwException(Translator.get("platform_plugin_not_exit") + PLUGIN_DOWNLOAD_URL);
        }
        return platform;
    }

    private String getUserPlatformInfo(String workspaceId) {
        try {
            String userId = SessionUtils.getUserId();
            if (StringUtils.isBlank(workspaceId) || StringUtils.isBlank(userId)) {
                return null;
            }
            User user = userMapper.selectByPrimaryKey(userId);
            if (StringUtils.isNotBlank(user.getPlatformInfo())) {
                return JSON.toJSONString(JSON.parseMap(user.getPlatformInfo()).get(workspaceId));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    public Map getFrontendMetaDataConfig(PluginWithBLOBs plugin, String configName) {
        Map metaData = JSON.parseMap(plugin.getFormScript());
        Map config = (Map) metaData.get(configName);
        config.put("id", metaData.get("id"));
        config.put("key", metaData.get("key"));
        return config;
    }

    public void getImage(InputStream in, HttpServletResponse response) {
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
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
            // 删除文件
            getPluginManager().getClassLoader(id).getStorageStrategy().delete();
            kafkaTemplate.send(KafkaTopicConstants.PLATFORM_PLUGIN, "DELETE:" + id);
        } catch (IOException e) {
            LogUtil.error(e);
        }
        this.unload(id);
    }

    public Platform getPlatFormInstance(String pluginId, Map IntegrationConfig) {
        PlatformRequest request = new PlatformRequest();
        request.setIntegrationConfig(JSON.toJSONString(IntegrationConfig));
        return getPluginManager().getPlatform(pluginId, request);
    }

    public Platform getPlatFormInstance(String pluginId, String integrationConfig) {
        PlatformRequest request = new PlatformRequest();
        request.setIntegrationConfig(integrationConfig);
        return getPluginManager().getPlatform(pluginId, request);
    }

    public void validateIntegration(String pluginId, Map integrationConfig) {
        Platform platform = getPlatFormInstance(pluginId, integrationConfig);
        platform.validateIntegrationConfig();
    }

    public void validateProjectConfig(String pluginId, Map projectConfig) {
        Platform platform = getPlatformByPluginId(pluginId);
        platform.validateProjectConfig(JSON.toJSONString(projectConfig));
    }

    private Platform getPlatformByPluginId(String pluginId) {
        PluginMetaInfo pluginMetaInfo = getPluginManager().getPluginMetaInfo(pluginId);
        IntegrationRequest integrationRequest = new IntegrationRequest();
        integrationRequest.setPlatform(pluginMetaInfo.getKey());
        integrationRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        ServiceIntegration serviceIntegration = baseIntegrationService.get(integrationRequest);
        Platform platform = getPlatFormInstance(pluginId, serviceIntegration.getConfiguration());
        return platform;
    }

    public void validateAccountConfig(String pluginId, Map accountConfig) {
        Platform platform = getPlatformByPluginId(pluginId);
        platform.validateUserConfig(JSON.toJSONString(accountConfig));
    }

    public List<SelectOption> getPlatformOptions() {
        List<SelectOption> options = getPluginManager().getPluginMetaInfoList()
                .stream()
                .map(pluginMetaInfo -> new SelectOption(pluginMetaInfo.getLabel(), pluginMetaInfo.getKey()))
                .collect(Collectors.toList());
        List<ServiceIntegration> integrations = baseIntegrationService.getAll(SessionUtils.getCurrentWorkspaceId());
        // 过滤掉服务集成中没有的选项
        return options.stream()
                .filter(option ->
                    integrations.stream()
                            .filter(integration -> StringUtils.equals(integration.getPlatform(), option.getValue()))
                            .collect(Collectors.toList()).size() > 0
                )
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getThirdPartTemplateSupportPlatform() {
        List<PluginMetaInfo> pluginMetaInfoList = getPluginManager().getPluginMetaInfoList();
        return pluginMetaInfoList.stream()
                .filter(PluginMetaInfo::isThirdPartTemplateSupport)
                .map(PluginMetaInfo::getKey)
                .collect(Collectors.toList());
    }

    public void loadPlugin(String pluginId) {
        if (getPluginManager().getClassLoader(pluginId) == null) {
            // 如果没有加载才加载
            InputStream pluginJar = basePluginService.getPluginJar(pluginId);
            PluginManagerUtil.loadPlugin(pluginId, getPluginManager(), pluginJar);
        }
    }


    /**
     * 卸载插件
     * @param pluginId
     */
    public void unload(String pluginId) {
        getPluginManager().deletePlugin(pluginId);
    }
}
