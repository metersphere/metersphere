package io.metersphere.api.service;

import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestEnvironmentService {

    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;

    public List<ApiTestEnvironmentWithBLOBs> list(String projectId) {
        ApiTestEnvironmentExample example =new ApiTestEnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
    }

    public ApiTestEnvironmentWithBLOBs get(String id) {
        return apiTestEnvironmentMapper.selectByPrimaryKey(id);
    }

    public void delete(String id) {
        apiTestEnvironmentMapper.deleteByPrimaryKey(id);
    }

    public void update(ApiTestEnvironmentWithBLOBs apiTestEnvironment) {
        apiTestEnvironmentMapper.updateByPrimaryKeySelective(apiTestEnvironment);
    }

    public String add(ApiTestEnvironmentWithBLOBs apiTestEnvironmentWithBLOBs) {
        apiTestEnvironmentWithBLOBs.setId(UUID.randomUUID().toString());
        apiTestEnvironmentMapper.insert(apiTestEnvironmentWithBLOBs);
        return apiTestEnvironmentWithBLOBs.getId();
    }
}
