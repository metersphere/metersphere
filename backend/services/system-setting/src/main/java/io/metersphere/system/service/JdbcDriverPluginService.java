package io.metersphere.system.service;

import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.sdk.OptionDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class JdbcDriverPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private BasePluginService basePluginService;
    @Resource
    private ProjectMapper projectMapper;

    public static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String DRIVER_OPTION_SEPARATOR = "&";
    public static final String SYSTEM_PLUGIN_ID = "system";


    public List<String> getJdbcDriverClass(String orgId) {
        List<Plugin> plugins = basePluginService.getOrgEnabledPlugins(orgId, PluginScenarioType.JDBC_DRIVER);
        List<Class> drivers = new ArrayList<>(10);
        for (Plugin plugin : plugins) {
            drivers.addAll(pluginLoadService.getMsPluginManager().getExtensionClasses(Driver.class, plugin.getId()));
        }

        List<String> pluginDriverClassNames = drivers.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
        if (!pluginDriverClassNames.contains(MYSQL_DRIVER_CLASS_NAME)) {
            // 如果不包含新版本 mysql 的驱动，则添加内置的 mysql 驱动
            pluginDriverClassNames.add(MYSQL_DRIVER_CLASS_NAME);
        }
        return  pluginDriverClassNames;
    }

    /**
     * 获取所有的 JDBC 驱动的实现类选项
     * @return
     */
    public List<OptionDTO> getJdbcDriverOption(String orgId) {
        List<Plugin> plugins = basePluginService.getOrgEnabledPlugins(orgId, PluginScenarioType.JDBC_DRIVER);
        List<OptionDTO> options = new ArrayList<>();
        for (Plugin plugin : plugins) {
            List<Class<? extends Driver>> extensionClasses = pluginLoadService.getMsPluginManager().getExtensionClasses(Driver.class, plugin.getId());
            extensionClasses.forEach(driver -> {
                options.add(new OptionDTO(plugin.getId() + DRIVER_OPTION_SEPARATOR + driver.getName(), driver.getName()));
            });
        }
        List<String> pluginDriverClassNames = options.stream().map(OptionDTO::getName).toList();
        if (!pluginDriverClassNames.contains(MYSQL_DRIVER_CLASS_NAME)) {
            // 如果不包含新版本 mysql 的驱动，则添加内置的 mysql 驱动
            options.add(new OptionDTO(SYSTEM_PLUGIN_ID + DRIVER_OPTION_SEPARATOR + MYSQL_DRIVER_CLASS_NAME, MYSQL_DRIVER_CLASS_NAME));
        }
        return  options;
    }

    public Plugin wrapperPlugin(Plugin plugin) {
        plugin.setScenario(PluginScenarioType.JDBC_DRIVER.name());
        plugin.setXpack(false);
        return plugin;
    }

    public Driver getDriverByOptionId(String driverId) {
        String[] split = driverId.split(DRIVER_OPTION_SEPARATOR);
        String pluginId = split[0];
        String className = split[1];
        if (StringUtils.equals(pluginId, SYSTEM_PLUGIN_ID)) {
            try {
               return (Driver) Class.forName(MYSQL_DRIVER_CLASS_NAME).getConstructor().newInstance();
            } catch (Exception e) {
                LogUtils.error(e);
                throw new MSException(e);
            }
        }
        List<Driver> extensions = pluginLoadService.getMsPluginManager().getExtensions(Driver.class, pluginId);
        return extensions.stream().filter(driver -> StringUtils.equals(driver.getClass().getName(), className))
                .findFirst()
                .orElseThrow(() -> new MSException("未找到对应的驱动"));
    }

    public List<ApiExecuteFileInfo> getFileInfoByProjectId(String projectId) {
        return basePluginService.getOrgEnabledPlugins(projectMapper.selectByPrimaryKey(projectId).getOrganizationId(), PluginScenarioType.JDBC_DRIVER)
                .stream().map(plugin -> {
                    ApiExecuteFileInfo apiExecuteFileInfo = new ApiExecuteFileInfo();
                    apiExecuteFileInfo.setFileId(plugin.getId() + "_" + plugin.getUpdateTime());
                    apiExecuteFileInfo.setFileName(plugin.getFileName());
                    return apiExecuteFileInfo;
                }).toList();

    }
}
