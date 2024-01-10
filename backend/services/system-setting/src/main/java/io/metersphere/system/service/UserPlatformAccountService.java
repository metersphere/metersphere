package io.metersphere.system.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
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
        } else {
            userExtend.setPlatformInfo(JSON.toJSONBytes(platformInfo));
            userExtendMapper.updateByPrimaryKeySelective(userExtend);
        }
        LogDTO dto = LogDTOBuilder.builder()
                .projectId(OperationLogConstants.SYSTEM)
                .organizationId(OperationLogConstants.SYSTEM)
                .type(OperationLogType.UPDATE.name())
                .module(OperationLogModule.PERSONAL_INFORMATION_APIKEYS)
                .method(HttpMethodConstants.GET.name())
                .path("/user/platform/save")
                .sourceId(userId)
                .originalValue(JSON.toJSONBytes(userExtend))
                .build().getLogDTO();
        operationLogService.add(dto);
    }

    public Map<String, Object> get(String userId) {
        UserExtend userExtend = userExtendMapper.selectByPrimaryKey(userId);
        if (userExtend == null || userExtend.getPlatformInfo() == null) {
            return new HashMap<>();
        }
        return JSON.parseMap(new String(userExtend.getPlatformInfo()));
    }
}
