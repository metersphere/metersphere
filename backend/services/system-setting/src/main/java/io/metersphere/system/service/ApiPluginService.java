package io.metersphere.system.service;

import io.metersphere.plugin.api.spi.AbstractApiPlugin;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.plugin.api.spi.AbstractProtocolPlugin;
import io.metersphere.plugin.api.spi.MsTestElement;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.PluginScenarioType;
import io.metersphere.sdk.dto.api.task.ApiExecuteFileInfo;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.dto.ProtocolDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiPluginService {

    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private BasePluginService basePluginService;
    @Resource
    private ProjectMapper projectMapper;

    /**
     * 获取协议插件的的协议列表
     *
     * @param orgId
     * @return
     */
    public List<ProtocolDTO> getProtocols(String orgId) {
        List<PluginWrapper> pluginWrappers = getOrgProtocolPluginWrappers(orgId);

        List protocols = new ArrayList<ProtocolDTO>();
        pluginWrappers.forEach(pluginWrapper -> {
            try {
                // 获取插件中 MsTestElement 的实现类
                List<Class<? extends MsTestElement>> extensionClasses = pluginWrapper.getPluginManager()
                        .getExtensionClasses(MsTestElement.class, pluginWrapper.getPluginId());

                AbstractProtocolPlugin protocolPlugin = ((AbstractProtocolPlugin) pluginWrapper.getPlugin());
                ProtocolDTO protocolDTO = new ProtocolDTO();
                protocolDTO.setProtocol(protocolPlugin.getProtocol());
                protocolDTO.setPluginId(pluginWrapper.getPluginId());
                if (CollectionUtils.isNotEmpty(extensionClasses)) {
                    protocolDTO.setPolymorphicName(extensionClasses.getFirst().getSimpleName());
                }
                if (StringUtils.isNoneBlank(protocolDTO.getProtocol(), protocolDTO.getPolymorphicName())) {
                    protocols.add(protocolDTO);
                }
            } catch (Exception e) {
                LogUtils.error(e);
            }
        });
        return protocols;
    }

    public List<ProtocolDTO> getProtocolsByProjectId(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return new ArrayList<>();
        } else {
            return this.getProtocols(project.getOrganizationId());
        }
    }

    private List<PluginWrapper> getOrgProtocolPluginWrappers(String orgId) {
        return getOrgApiPluginWrappers(orgId).stream()
                .filter(plugin -> plugin.getPlugin() instanceof AbstractProtocolPlugin)
                .toList();
    }

    public List<PluginWrapper> getOrgApiPluginWrappers(String orgId) {
        // 查询组织下有权限的插件
        Set<String> pluginIds = basePluginService.getOrgEnabledPlugins(orgId, PluginScenarioType.API_PROTOCOL)
                .stream()
                .map(Plugin::getId)
                .collect(Collectors.toSet());

        // 过滤协议插件
        List<PluginWrapper> plugins = pluginLoadService.getMsPluginManager().getPlugins();
        List<PluginWrapper> pluginWrappers = plugins.stream()
                .filter(plugin -> pluginIds.contains(plugin.getPluginId()) && plugin.getPlugin() instanceof AbstractApiPlugin)
                .toList();
        return pluginWrappers;
    }

    /**
     * 返回 MsTestElement 实现类与插件 ID 的映射
     *
     * @return
     */
    public Map<Class<? extends AbstractMsTestElement>, String> getTestElementPluginMap() {
        Map<Class<? extends AbstractMsTestElement>, String> testElementPluginMap = new HashMap<>();
        List<PluginWrapper> plugins = pluginLoadService.getMsPluginManager().getPlugins();
        for (PluginWrapper plugin : plugins) {
            if (plugin.getPlugin() instanceof AbstractApiPlugin) {
                List<Class<? extends MsTestElement>> extensionClasses = plugin.getPluginManager().getExtensionClasses(MsTestElement.class, plugin.getPluginId());
                extensionClasses.forEach(clazz -> testElementPluginMap.put((Class<? extends AbstractMsTestElement>) clazz, plugin.getPluginId()));
            }
        }
        return testElementPluginMap;
    }

    /**
     * 返回 MsTestElement 实现类与接口协议的映射
     *
     * @return
     */
    public Map<Class<? extends AbstractMsTestElement>, String> getTestElementProtocolMap() {
        // 过滤协议插件
        List<PluginWrapper> protocolPlugins = pluginLoadService.getMsPluginManager()
                .getPlugins()
                .stream()
                .filter(plugin -> plugin.getPlugin() instanceof AbstractProtocolPlugin)
                .toList();

        Map<Class<? extends AbstractMsTestElement>, String> testElementProtocolMap = new HashMap<>();
        for (PluginWrapper plugin : protocolPlugins) {
            List<Class<? extends MsTestElement>> extensionClasses = plugin.getPluginManager().getExtensionClasses(MsTestElement.class);
            extensionClasses.forEach(clazz -> testElementProtocolMap.put((Class<? extends AbstractMsTestElement>) clazz, ((AbstractProtocolPlugin) plugin.getPlugin()).getProtocol()));
        }
        return testElementProtocolMap;
    }

    public List<ApiExecuteFileInfo> getFileInfoByProjectId(String projectId) {
        return basePluginService.getOrgEnabledPlugins(projectMapper.selectByPrimaryKey(projectId).getOrganizationId(), PluginScenarioType.API_PROTOCOL)
                .stream().map(plugin -> {
                    ApiExecuteFileInfo apiExecuteFileInfo = new ApiExecuteFileInfo();
                    apiExecuteFileInfo.setFileId(plugin.getId() + "_" + plugin.getUpdateTime());
                    apiExecuteFileInfo.setFileName(plugin.getFileName());
                    return apiExecuteFileInfo;
                }).toList();

    }
}
