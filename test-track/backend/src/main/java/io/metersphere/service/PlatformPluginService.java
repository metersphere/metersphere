package io.metersphere.service;

import io.metersphere.base.domain.User;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.i18n.Translator;
import io.metersphere.platform.api.Platform;
import io.metersphere.platform.api.PluginMetaInfo;
import io.metersphere.base.domain.PluginWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.commons.constants.PluginScenario;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.platform.domain.GetOptionRequest;
import io.metersphere.platform.domain.PlatformRequest;
import io.metersphere.platform.domain.SelectOption;

import io.metersphere.platform.loader.PlatformPluginManager;
import io.metersphere.request.IntegrationRequest;
import io.metersphere.request.PlatformOptionRequest;
import io.metersphere.utils.PluginManagerUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PlatformPluginService {

    @Resource
    private BasePluginService basePluginService;
    @Resource
    private BaseIntegrationService baseIntegrationService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private UserMapper userMapper;

    public static final String PLUGIN_DOWNLOAD_URL = "https://github.com/metersphere/metersphere-platform-plugin";

    private PlatformPluginManager pluginManager;

    public synchronized PlatformPluginManager getPluginManager() {
        if (pluginManager == null) {
            pluginManager = new PlatformPluginManager();
        }
        return pluginManager;
    }

    /**
     * 查询所有平台插件并加载
     */
    public void loadPlatFormPlugins() {
        List<PluginWithBLOBs> plugins = basePluginService.getPlugins(PluginScenario.platform.name());
        PluginManagerUtil.loadPlugins(getPluginManager(), plugins);
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
    public void unloadPlugin(String pluginId) {
        getPluginManager().deletePlugin(pluginId);
    }

    public boolean isThirdPartTemplateSupport(String platform) {
        if (StringUtils.isBlank(platform)) {
            return false;
        }
        PluginMetaInfo pluginMetaInfo = pluginManager.getPluginMetaInfoByKey(platform);
        if (PlatformPluginService.isPluginPlatform(platform) && pluginMetaInfo == null) {
            return false;
        }
        return pluginMetaInfo == null ? false : pluginMetaInfo.isThirdPartTemplateSupport();
    }

    public Platform getPlatform(String platformKey, String workspaceId) {
        IntegrationRequest integrationRequest = new IntegrationRequest();
        integrationRequest.setPlatform(platformKey);
        integrationRequest.setWorkspaceId(StringUtils.isBlank(workspaceId) ? SessionUtils.getCurrentWorkspaceId() : workspaceId);
        ServiceIntegration serviceIntegration = baseIntegrationService.get(integrationRequest);

        PlatformRequest pluginRequest = new PlatformRequest();
        pluginRequest.setWorkspaceId(workspaceId);
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

    public Platform getPlatform(String platformKey) {
       return this.getPlatform(platformKey, null);
    }


    public static String getCompatibleProjectConfig(Project project) {
        String issueConfig = project.getIssueConfig();
        if (StringUtils.isBlank(issueConfig)) {
            return StringUtils.EMPTY;
        }
        Map map = JSON.parseMap(issueConfig);
        compatibleProjectKey(map, "jiraKey", project.getJiraKey());
        compatibleProjectKey(map, "tapdId", project.getTapdId());
        compatibleProjectKey(map, "azureDevopsId", project.getAzureDevopsId());
        compatibleProjectKey(map, "zentaoId", project.getZentaoId());
        map.put("thirdPartTemplate", project.getThirdPartTemplate());
        return JSON.toJSONString(map);
    }

    private static void compatibleProjectKey(Map map, String name, String compatibleValue) {
        if (map.get(name) == null || StringUtils.isBlank(map.get(name).toString())) {
            // 如果配置里面缺陷对应平台的项目ID则，即使用旧数据的项目ID
            map.put(name, compatibleValue);
        }
    }

    public static boolean isPluginPlatform(String platform) {
        if (StringUtils.equalsAnyIgnoreCase(platform,
                IssuesManagePlatform.Tapd.name(), IssuesManagePlatform.AzureDevops.name(), IssuesManagePlatform.Local.name())) {
            return false;
        }
        return true;
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

    public List<SelectOption> getFormOption(PlatformOptionRequest request) {
        Platform platform = getPlatform(request.getPlatform(), request.getWorkspaceId());
        GetOptionRequest getOptionRequest = new GetOptionRequest();
        getOptionRequest.setOptionMethod(request.getOptionMethod());
        String projectConfig = getCompatibleProjectConfig(baseProjectService.getProjectById(request.getProjectId()));
        getOptionRequest.setProjectConfig(projectConfig);
        getOptionRequest.setQuery(request.getQuery());
        try {
            return platform.getFormOptions(getOptionRequest);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
