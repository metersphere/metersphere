package io.metersphere.api.service;

import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
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
        checkEnvironmentExist(apiTestEnvironmentWithBLOBs);
        apiTestEnvironmentMapper.insert(apiTestEnvironmentWithBLOBs);
        return apiTestEnvironmentWithBLOBs.getId();
    }

    private void checkEnvironmentExist (ApiTestEnvironmentWithBLOBs environment) {
        if (environment.getName() != null) {
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            example.createCriteria()
                    .andNameEqualTo(environment.getName())
                    .andProjectIdEqualTo(environment.getProjectId());
            if (apiTestEnvironmentMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("api_test_environment_already_exists"));
            }
        }
    }
}
