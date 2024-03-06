package io.metersphere.api.service;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.plugin.api.dto.ApiPluginOptionsRequest;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.plugin.api.spi.AbstractApiPlugin;
import io.metersphere.plugin.api.spi.AbstractProtocolPlugin;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentDTO;
import io.metersphere.project.dto.environment.datasource.DataSource;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jianxing
 * @date : 2023-11-6
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestService {

    @Resource
    private ApiPluginService apiPluginService;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private ExtEnvironmentMapper extEnvironmentMapper;
    @Resource
    private EnvironmentService environmentService;

    public List<ProtocolDTO> getProtocols(String orgId) {
        List<ProtocolDTO> protocols = apiPluginService.getProtocols(orgId);
        // 将 http 协议放最前面
        ProtocolDTO protocolDTO = new ProtocolDTO();
        protocolDTO.setProtocol(ApiConstants.HTTP_PROTOCOL);
        protocolDTO.setPolymorphicName(MsHTTPElement.class.getSimpleName());
        protocols.addFirst(protocolDTO);
        return protocols;
    }

    public List<ApiPluginSelectOption> getFormOptions(ApiTestPluginOptionRequest request) {
        // 获取组织下有权限的接口插件
        List<PluginWrapper> pluginWrappers = apiPluginService.getOrgApiPluginWrappers(request.getOrgId());
        PluginWrapper pluginWrapper = pluginWrappers.stream()
                .filter(item -> StringUtils.equals(item.getPluginId(), request.getPluginId()))
                .findFirst()
                .orElse(null);
        if (pluginWrapper == null || !(pluginWrapper.getPlugin() instanceof AbstractApiPlugin)) {
            // 插件不存在或者非接口插件，抛出异常
            checkResourceExist(null);
        }
        ApiPluginOptionsRequest optionsRequest = BeanUtils.copyBean(new ApiPluginOptionsRequest(), request);
        assert pluginWrapper != null;
        return ((AbstractApiPlugin) pluginWrapper.getPlugin()).getPluginOptions(optionsRequest);
    }

    public Object getApiProtocolScript(String pluginId) {
        PluginWrapper pluginWrapper = pluginLoadService.getPluginWrapper(pluginId);
        checkResourceExist(pluginWrapper);
        if (pluginWrapper.getPlugin() instanceof AbstractProtocolPlugin protocolPlugin) {
            return pluginLoadService.getPluginScript(pluginId, protocolPlugin.getApiProtocolScriptId());
        } else {
            // 插件不存在或者非接口插件，抛出异常
            return checkResourceExist(null);
        }
    }

    public Object checkResourceExist(PluginWrapper pluginWrapper) {
        return ServiceUtils.checkResourceExist(pluginWrapper, "permission.system_plugin.name");
    }

    public List<Environment> getEnvList(String projectId) {
        return extEnvironmentMapper.selectByKeyword(null, false, projectId);
    }

    public EnvironmentConfig getEnvironmentConfig(String environmentId) {
        EnvironmentConfig environmentConfig = environmentService.getEnvironmentConfig(environmentId);
        // 数据脱敏
        EnvironmentConfig newEnvironmentConfig = new EnvironmentConfig();
        newEnvironmentConfig.setHttpConfig(environmentConfig.getHttpConfig());
        newEnvironmentConfig.setCommonVariables(environmentConfig.getCommonVariables());
        List<DataSource> dataSources = environmentConfig.getDataSources().stream().map(dataSource -> {
            DataSource newDataSource = new DataSource();
            newDataSource.setId(dataSource.getId());
            newDataSource.setDataSource(dataSource.getDataSource());
            return newDataSource;
        }).toList();
        newEnvironmentConfig.setAuthConfig(null);
        newEnvironmentConfig.setPreProcessorConfig(null);
        newEnvironmentConfig.setPostProcessorConfig(null);
        newEnvironmentConfig.setAssertionConfig(null);
        newEnvironmentConfig.setDataSources(dataSources);
        return newEnvironmentConfig;
    }
}