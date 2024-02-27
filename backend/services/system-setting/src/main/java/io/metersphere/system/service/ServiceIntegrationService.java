package io.metersphere.system.service;

import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.ServiceIntegrationExample;
import io.metersphere.system.dto.ServiceIntegrationDTO;
import io.metersphere.system.dto.request.ServiceIntegrationUpdateRequest;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.metersphere.system.controller.result.SystemResultCode.SERVICE_INTEGRATION_EXIST;

/**
 * @author jianxing
 * @date : 2023-8-9
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ServiceIntegrationService {
    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private PluginLoadService pluginLoadService;
    @Resource
    private PlatformPluginService platformPluginService;
    @Resource
    private BasePluginService basePluginService;
    @Resource
    private PluginService pluginService;

    public static final String PLUGIN_IMAGE_GET_PATH = "/plugin/image/%s?imagePath=%s";

    public List<ServiceIntegrationDTO> list(String organizationId) {
        // 查询服务集成已配置数据
        Map<String, ServiceIntegration> serviceIntegrationMap = getServiceIntegrationByOrgId(organizationId).stream()
                .collect(Collectors.toMap(ServiceIntegration::getPluginId, i -> i));

        List<Plugin> plugins = platformPluginService.getOrgEnabledPlatformPlugins(organizationId);
        return plugins.stream().map(plugin -> {
            AbstractPlatformPlugin msPlugin = (AbstractPlatformPlugin) pluginLoadService.getPluginWrapper(plugin.getId()).getPlugin();
            // 获取插件基础信息
            ServiceIntegrationDTO serviceIntegrationDTO = new ServiceIntegrationDTO();
            serviceIntegrationDTO.setTitle(msPlugin.getName());
            serviceIntegrationDTO.setEnable(false);
            serviceIntegrationDTO.setConfig(false);
            serviceIntegrationDTO.setDescription(msPlugin.getDescription());
            serviceIntegrationDTO.setLogo(String.format(PLUGIN_IMAGE_GET_PATH, plugin.getId(), msPlugin.getLogo()));
            serviceIntegrationDTO.setPluginId(plugin.getId());
            ServiceIntegration serviceIntegration = serviceIntegrationMap.get(plugin.getId());
            if (serviceIntegration != null) {
                serviceIntegrationDTO.setConfiguration(JSON.parseMap(new String(serviceIntegration.getConfiguration())));
                serviceIntegrationDTO.setId(serviceIntegration.getId());
                serviceIntegrationDTO.setEnable(serviceIntegration.getEnable());
                serviceIntegrationDTO.setConfig(true);
                serviceIntegrationDTO.setOrganizationId(serviceIntegration.getOrganizationId());
            }
            return serviceIntegrationDTO;
        }).toList();
    }

    public List<ServiceIntegration> getServiceIntegrationByOrgId(String organizationId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(organizationId);
        return serviceIntegrationMapper.selectByExampleWithBLOBs(example);
    }

    public ServiceIntegration add(ServiceIntegrationUpdateRequest request) {
        basePluginService.checkPluginEnableAndPermission(request.getPluginId(), request.getOrganizationId());
        ServiceIntegration serviceIntegration = new ServiceIntegration();
        BeanUtils.copyBean(serviceIntegration, request);
        serviceIntegration.setId(IDGenerator.nextStr());
        serviceIntegration.setConfiguration(JSON.toJSONBytes(request.getConfiguration()));
        checkAddExist(serviceIntegration);
        serviceIntegrationMapper.insert(serviceIntegration);
        return serviceIntegration;
    }

    public ServiceIntegration update(ServiceIntegrationUpdateRequest request) {
        checkResourceExist(request.getId());
        ServiceIntegration originServiceIntegration = serviceIntegrationMapper.selectByPrimaryKey(request.getId());
        basePluginService.checkPluginEnableAndPermission(originServiceIntegration.getPluginId(), originServiceIntegration.getOrganizationId());
        ServiceIntegration serviceIntegration = new ServiceIntegration();
        // 组织不能修改
        serviceIntegration.setOrganizationId(null);
        BeanUtils.copyBean(serviceIntegration, request);
        if (request.getConfiguration() != null) {
            serviceIntegration.setConfiguration(JSON.toJSONBytes(request.getConfiguration()));
        }
        serviceIntegrationMapper.updateByPrimaryKeySelective(serviceIntegration);
        return serviceIntegration;
    }

    private ServiceIntegration checkResourceExist(String id) {
        return ServiceUtils.checkResourceExist(serviceIntegrationMapper.selectByPrimaryKey(id), "permission.service_integration.name");
    }

    public void delete(String id) {
        checkResourceExist(id);
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(id);
        basePluginService.checkPluginEnableAndPermission(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId());
        serviceIntegrationMapper.deleteByPrimaryKey(id);
    }

    private void checkAddExist(ServiceIntegration serviceIntegration) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(serviceIntegration.getOrganizationId())
                .andPluginIdEqualTo(serviceIntegration.getPluginId());
        if (CollectionUtils.isNotEmpty(serviceIntegrationMapper.selectByExample(example))) {
            throw new MSException(SERVICE_INTEGRATION_EXIST);
        }
    }

    public void validate(String pluginId, String orgId, Map<String, String> serviceIntegrationInfo) {
        pluginService.checkResourceExist(pluginId);
        Platform platform = platformPluginService.getPlatform(pluginId, orgId, JSON.toJSONString(serviceIntegrationInfo));
        platform.validateIntegrationConfig();
    }

    public void validate(String id) {
        ServiceIntegration serviceIntegration = checkResourceExist(id);
        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId());
        platform.validateIntegrationConfig();
    }

    public ServiceIntegration get(String id) {
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(id);
        basePluginService.checkPluginEnableAndPermission(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId());
        return serviceIntegration;
    }

    public Object getPluginScript(String pluginId) {
        pluginService.checkResourceExist(pluginId);
        AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getPluginWrapper(pluginId).getPlugin();
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getIntegrationScriptId());
    }
}