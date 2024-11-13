package io.metersphere.system.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.plugin.sdk.spi.MsPlugin;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.OperationLogConstants;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.UserExtend;
import io.metersphere.system.dto.builder.LogDTOBuilder;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.UserExtendMapper;
import jakarta.annotation.Resource;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional(rollbackFor = Exception.class)
public class UserPlatformAccountService {
    @Resource
    private BasePluginService basePluginService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private UserExtendMapper userExtendMapper;
    @Resource
    private OperationLogService operationLogService;

    public Map<String, Object> getAccountInfoList() {
        // 当前系统下所有的开启的三方的插件  动态获取内容
        List<Plugin> plugins = basePluginService.getEnabledPlugins(PluginScenarioType.PLATFORM);
        //将结果放到一个map中  key为插件id  value为账号信息
        Map<String, Object> accountInfoMap = new HashMap<>();
        plugins.forEach(plugin -> {
            Object accountInfo = getAccountInfo(plugin.getId());
            PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(plugin.getId());
            MsPlugin msPlugin = (MsPlugin) pluginWrapper.getPlugin();
            ((Map) accountInfo).put("pluginName", msPlugin.getName());
            ((Map) accountInfo).put("pluginLogo", ((AbstractPlatformPlugin) msPlugin).getLogo());
            accountInfoMap.put(plugin.getId(), accountInfo);
        });
        return accountInfoMap;
    }

    private Object getAccountInfo(String pluginId) {
        // 获取插件的账号信息
        AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getPluginWrapper(pluginId).getPlugin();
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getAccountScriptId());
    }

    public void validate(String pluginId, String orgId, Map<String, String> userPlatformConfig) {
        // 获取组织服务集成信息
        Platform platform = platformPluginService.getPlatform(pluginId, orgId);
        platform.validateUserConfig(JSON.toJSONString(userPlatformConfig));
    }

    public void save(Map<String, Object> platformInfo, String userId) {
        UserExtend userExtend = userExtendMapper.selectByPrimaryKey(userId);
        if (userExtend == null) {
            userExtend = new UserExtend();
            userExtend.setId(userId);
            userExtend.setPlatformInfo(JSON.toJSONBytes(platformInfo));
            userExtendMapper.insertSelective(userExtend);
        } else if (userExtend.getPlatformInfo() == null) {
            userExtend.setPlatformInfo(JSON.toJSONBytes(platformInfo));
            userExtendMapper.updateByPrimaryKeySelective(userExtend);
        } else {
            // noinspection unchecked
            Map<String, Object> originalUserPlatformInfo = JSON.parseMap(new String(userExtend.getPlatformInfo()));
            originalUserPlatformInfo.putAll(platformInfo);
            userExtend.setPlatformInfo(JSON.toJSONBytes(originalUserPlatformInfo));
            userExtendMapper.updateByPrimaryKeySelective(userExtend);
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .organizationId(OperationLogConstants.SYSTEM)
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PERSONAL_INFORMATION_TRIPARTITE)
                .method(HttpMethodConstants.GET.name())
                .path("/user/platform/save")
                .sourceId(userId)
                .originalValue(JSON.toJSONBytes(userExtend))
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    /**
     * 获取个人三方平台账号
     *
     * @param userId 用户ID
     * @param orgId  组织ID
     * @return 三方平台账号
     */
    public Map<String, Object> get(String userId, String orgId) {
        UserExtend userExtend = userExtendMapper.selectByPrimaryKey(userId);
        if (userExtend == null || userExtend.getPlatformInfo() == null) {
            return null;
        }
        // noinspection unchecked
        Map<String, Object> userPlatformInfo = JSON.parseMap(new String(userExtend.getPlatformInfo()));
        // 获取组织用户集成信息
        // noinspection unchecked
        return (Map<String, Object>) userPlatformInfo.get(orgId);
    }

    /**
     * 获取插件个人三方平台账号
     *
     * @param userId 用户ID
     * @param orgId  组织ID
     * @return 三方平台账号
     */
    public Map<String, Object> getPluginUserPlatformConfig(String pluginId, String orgId, String userId) {
        // 获取组织用户集成信息
        Map<String, Object> userPlatformInfo = get(userId, orgId);
        if (userPlatformInfo == null) {
            return null;
        }
        // noinspection unchecked
        return (Map<String, Object>) userPlatformInfo.get(pluginId);
    }

    public String getPlatformUserName(String uerId, String orgId, String pluginId) {
        Map<String, Object> pluginUserPlatformConfig = getPluginUserPlatformConfig(pluginId, orgId, uerId);
        if (pluginUserPlatformConfig == null) {
            return null;
        }
        return (String) pluginUserPlatformConfig.get("nickName");
    }
}
