package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.ServiceIntegrationExample;
import io.metersphere.base.mapper.ServiceIntegrationMapper;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class IntegrationService {

    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;

    public ServiceIntegration save(ServiceIntegration service) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(service.getOrganizationId())
                .andPlatformEqualTo(service.getPlatform());
        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(list)) {
            serviceIntegrationMapper.updateByExampleSelective(service, example);
            return list.get(0);
        } else {
            service.setId(UUID.randomUUID().toString());
            serviceIntegrationMapper.insertSelective(service);
            return service;
        }
    }

    public ServiceIntegration get(IntegrationRequest request) {
        String platform = request.getPlatform();
        String orgId = request.getOrgId();
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        ServiceIntegrationExample.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(platform)) {
            criteria.andPlatformEqualTo(platform);
        }

        if (StringUtils.isNotBlank(orgId)) {
            criteria.andOrganizationIdEqualTo(orgId);
        }

        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExampleWithBLOBs(example);
        return CollectionUtils.isEmpty(list) ? new ServiceIntegration() : list.get(0);
    }

    public void delete(IntegrationRequest request) {
        String platform = request.getPlatform();
        String orgId = request.getOrgId();
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andOrganizationIdEqualTo(orgId)
                .andPlatformEqualTo(platform);
        serviceIntegrationMapper.deleteByExample(example);
        // 删除项目关联的id/key
        extProjectMapper.removeIssuePlatform(platform, orgId);
    }

    public List<ServiceIntegration> getAll(String orgId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andOrganizationIdEqualTo(orgId);
        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExample(example);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    public String getLogDetails(String id) {
        ServiceIntegration ser = serviceIntegrationMapper.selectByPrimaryKey(id);
        if (ser != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(ser, SystemReference.serverColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ser.getId()), null, ser.getPlatform(), null, columns);
            return JSON.toJSONString(details);
        }
        return null;
    }
}
