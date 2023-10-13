package io.metersphere.service;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.domain.ServiceIntegrationExample;
import io.metersphere.base.mapper.ServiceIntegrationMapper;
import io.metersphere.base.mapper.ext.BaseProjectMapper;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.utils.JSON;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import io.metersphere.request.IntegrationRequest;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseIntegrationService {

    @Resource
    private ServiceIntegrationMapper serviceIntegrationMapper;
    @Resource
    private BaseProjectMapper baseProjectMapper;
    @Resource
    private MicroService microService;

    public ServiceIntegration save(ServiceIntegration service) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(service.getWorkspaceId())
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
        String workspaceId = request.getWorkspaceId();
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        ServiceIntegrationExample.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(platform)) {
            criteria.andPlatformEqualTo(platform);
        }

        if (StringUtils.isNotBlank(workspaceId)) {
            criteria.andWorkspaceIdEqualTo(workspaceId);
        }

        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExampleWithBLOBs(example);
        return CollectionUtils.isEmpty(list) ? new ServiceIntegration() : list.get(0);
    }

    public void delete(IntegrationRequest request) {
        String platform = request.getPlatform();
        String workspaceId = request.getWorkspaceId();
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andPlatformEqualTo(platform);
        serviceIntegrationMapper.deleteByExample(example);
        // 删除项目关联的id/key(目前只有jira/tapd/zentao/azure才有项目对接的key)
        if (StringUtils.equalsAny(platform, "Jira", "Tapd", "Zentao", "AzureDevops")) {
            baseProjectMapper.removeIssuePlatform(platform, workspaceId);
        }
    }

    public List<ServiceIntegration> getAll(String workspaceId) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExample(example);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    public String getLogDetails(String workspaceId, String platform) {
        ServiceIntegrationExample example = new ServiceIntegrationExample();
        example.createCriteria()
                .andWorkspaceIdEqualTo(workspaceId)
                .andPlatformEqualTo(platform);
        List<ServiceIntegration> list = serviceIntegrationMapper.selectByExampleWithBLOBs(example);
        if (!CollectionUtils.isEmpty(list)) {
            ServiceIntegration ser = list.get(0);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(ser, SystemReference.serverColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ser.getId()), null, ser.getPlatform(), null, columns);
            return JSON.toJSONString(details);
        } else {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(new ServiceIntegration(), SystemReference.serverColumns);
            OperatingLogDetails details = new OperatingLogDetails("", null, platform, null, columns);
            return JSON.toJSONString(details);
        }
    }

    public void authServiceIntegration(String workspaceId, String platform) {
        microService.getForData(MicroServiceName.TEST_TRACK, String.format("/issues/auth/%s/%s", workspaceId, platform));
    }
}
