package io.metersphere.system.service;

import io.metersphere.plugin.platform.api.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.api.Platform;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.service.PlatformPluginService;
import io.metersphere.sdk.service.PluginLoadService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.Plugin;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.ServiceIntegrationExample;
import io.metersphere.system.dto.ServiceIntegrationDTO;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public static final String PLUGIN_IMAGE_GET_PATH = "/plugin/image/%s?imagePath=%s";

    public List<ServiceIntegrationDTO> list(String organizationId) {
        // 查询服务集成已配置数据
        Map<String, ServiceIntegration> serviceIntegrationMap = getServiceIntegrationByOrgId(organizationId).stream()
                .collect(Collectors.toMap(ServiceIntegration::getPluginId, i -> i));

        List<Plugin> plugins = platformPluginService.getPlatformPlugins();
        return plugins.stream().map(plugin -> {
            AbstractPlatformPlugin msPluginInstance = pluginLoadService.getPlatformPluginInstance(plugin.getId());
            // 获取插件基础信息
            ServiceIntegrationDTO serviceIntegrationDTO = new ServiceIntegrationDTO();
            serviceIntegrationDTO.setTitle(msPluginInstance.getName());
            serviceIntegrationDTO.setEnable(false);
            serviceIntegrationDTO.setConfig(false);
            serviceIntegrationDTO.setDescription(msPluginInstance.getDescription());
            serviceIntegrationDTO.setLogo(String.format(PLUGIN_IMAGE_GET_PATH, plugin.getId(), msPluginInstance.getLogo()));
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

    private List<ServiceIntegration> getServiceIntegrationByOrgId(String organizationId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(organizationId);
        return serviceIntegrationMapper.selectByExampleWithBLOBs(example);
    }

    public ServiceIntegration add(ServiceIntegrationUpdateRequest request) {
        ServiceIntegration serviceIntegration = new ServiceIntegration();
        BeanUtils.copyBean(serviceIntegration, request);
        serviceIntegration.setId(UUID.randomUUID().toString());
        serviceIntegration.setConfiguration(JSON.toJSONBytes(request.getConfiguration()));
        checkAddExist(serviceIntegration);
        serviceIntegrationMapper.insert(serviceIntegration);
        return serviceIntegration;
    }

    public ServiceIntegration update(ServiceIntegrationUpdateRequest request) {
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

    public void delete(String id) {
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

    public void validate(String pluginId, Map<String, String> serviceIntegrationInfo) {
        Platform platform = platformPluginService.getPlatform(pluginId, StringUtils.EMPTY, JSON.toJSONString(serviceIntegrationInfo));
        platform.validateIntegrationConfig();
    }

    public void validate(String id) {
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(id);
        Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), StringUtils.EMPTY);
        platform.validateIntegrationConfig();
    }

    public ServiceIntegration get(String id) {
        return serviceIntegrationMapper.selectByPrimaryKey(id);
    }

    public Object getPluginScript(String pluginId) {
        AbstractPlatformPlugin platformPlugin = pluginLoadService.getImplInstance(pluginId, AbstractPlatformPlugin.class);
        return pluginLoadService.getPluginScriptContent(pluginId, platformPlugin.getIntegrationScriptId());
    }
}