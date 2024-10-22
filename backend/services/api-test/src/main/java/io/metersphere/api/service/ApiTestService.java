package io.metersphere.api.service;

import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.ApiTestPluginOptionRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.plugin.api.dto.ApiPluginOptionsRequest;
import io.metersphere.plugin.api.dto.ApiPluginSelectOption;
import io.metersphere.plugin.api.spi.AbstractApiPlugin;
import io.metersphere.plugin.api.spi.AbstractProtocolPlugin;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.project.dto.customfunction.CustomFunctionDTO;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.service.CustomFunctionService;
import io.metersphere.project.service.EnvironmentService;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectService;
import io.metersphere.sdk.constants.ProjectApplicationType;
import io.metersphere.sdk.constants.StorageType;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.file.FileRequest;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.TestResourcePool;
import io.metersphere.system.dto.ProtocolDTO;
import io.metersphere.system.service.ApiPluginService;
import io.metersphere.system.service.FileService;
import io.metersphere.system.service.PluginLoadService;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private FileService fileService;
    @Resource
    private CustomFunctionService customFunctionService;
    @Resource
    private ProjectService projectService;

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
        newEnvironmentConfig.setPreProcessorConfig(null);
        newEnvironmentConfig.setPostProcessorConfig(null);
        newEnvironmentConfig.setAssertionConfig(null);
        newEnvironmentConfig.setDataSources(environmentConfig.getDataSources());
        return newEnvironmentConfig;
    }

    public List<TestResourcePool> getPoolOption(String projectId) {
        //获取资源池
        return projectService.getPoolOption(projectId);
    }

    public String getPoolId(String projectId) {
        // 查询接口默认资源池
        ProjectApplication resourcePoolConfig = projectApplicationService.getByType(projectId, ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
        Map<String, Object> configMap = new HashMap<>();
        if (resourcePoolConfig != null && StringUtils.isNotBlank(resourcePoolConfig.getTypeValue())) {
            configMap.put(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name(), resourcePoolConfig.getTypeValue());
        }
        projectApplicationService.putResourcePool(projectId, configMap, "apiTest");
        if (configMap.isEmpty()) {
            return null;
        }
        return (String) configMap.get(ProjectApplicationType.API.API_RESOURCE_POOL_ID.name());
    }

    public void download(String path, HttpServletResponse response) {
        if (StringUtils.isBlank(path)) {
            return;
        }
        try {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String folder = path.substring(0, path.lastIndexOf("/"));
            FileRequest fileRequest = new FileRequest();
            fileRequest.setFileName(fileName);
            fileRequest.setFolder(folder);
            fileRequest.setStorage(StorageType.MINIO.name());

            byte[] bytes = fileService.download(fileRequest);
            fileWithResponse(bytes, fileName, response);
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }

    private void fileWithResponse(byte[] content, String fileName, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    public CommonScriptInfo getCommonScriptInfo(String scriptId) {
        CustomFunctionDTO customFunctionDTO = customFunctionService.get(scriptId);
        if (customFunctionDTO == null) {
            return null;
        }
        CommonScriptInfo commonScriptInfo = new CommonScriptInfo();
        commonScriptInfo.setScriptLanguage(customFunctionDTO.getType());
        commonScriptInfo.setScript(customFunctionDTO.getScript());
        commonScriptInfo.setName(customFunctionDTO.getName());
        commonScriptInfo.setId(customFunctionDTO.getId());
        if (StringUtils.isNotBlank(customFunctionDTO.getParams())) {
            commonScriptInfo.setParams(JSON.parseArray(customFunctionDTO.getParams(), KeyValueParam.class));
        }
        return commonScriptInfo;
    }
}