package io.metersphere.system.service;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.domain.ServiceIntegrationExample;
import io.metersphere.system.dto.ServiceIntegrationDTO;
import io.metersphere.system.mapper.ServiceIntegrationMapper;
import io.metersphere.system.request.ServiceIntegrationUpdateRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public List<ServiceIntegrationDTO> list(String organizationId) {
        return Arrays.asList(new ServiceIntegrationDTO());
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
        BeanUtils.copyBean(serviceIntegration, request);
        if (request.getConfiguration() != null) {
            serviceIntegration.setConfiguration(JSON.toJSONBytes(request.getConfiguration()));
        }
        serviceIntegrationMapper.updateByPrimaryKeySelective(serviceIntegration);
        return serviceIntegration;
    }

    public String delete(String id) {
        serviceIntegrationMapper.deleteByPrimaryKey(id);
        return id;
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

    public boolean validate(Map<String, String> serviceIntegrationInfo) {
        return serviceIntegrationInfo == null;
    }

    public boolean validate(String id) {
        ServiceIntegration serviceIntegration = serviceIntegrationMapper.selectByPrimaryKey(id);
        return serviceIntegration == null;
    }

    public ServiceIntegration get(String id) {
        return serviceIntegrationMapper.selectByPrimaryKey(id);
    }

    public Object getPluginScript(String pluginId) {
        return new ServiceIntegration();
    }
}