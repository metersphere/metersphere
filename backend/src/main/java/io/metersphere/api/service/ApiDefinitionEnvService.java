package io.metersphere.api.service;

import io.metersphere.base.domain.ApiDefinitionEnv;
import io.metersphere.base.domain.ApiDefinitionEnvExample;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.mapper.ApiDefinitionEnvMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionEnvService {
    @Resource
    private ApiDefinitionEnvMapper apiDefinitionEnvMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;

    public void insert(ApiDefinitionEnv env) {
        env.setId(UUID.randomUUID().toString());
        env.setCreateTime(System.currentTimeMillis());
        env.setUpdateTime(System.currentTimeMillis());
        ApiDefinitionEnv apiDefinitionEnv = this.get(env.getUserId());
        if (apiDefinitionEnv == null) {
            apiDefinitionEnvMapper.insert(env);
        } else {
            apiDefinitionEnv.setEnvId(env.getEnvId());
            apiDefinitionEnv.setUpdateTime(System.currentTimeMillis());
            apiDefinitionEnvMapper.updateByPrimaryKey(apiDefinitionEnv);
        }
    }

    public ApiDefinitionEnv get(String userId, String projectId) {
        ApiDefinitionEnvExample example = new ApiDefinitionEnvExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<ApiDefinitionEnv> list = apiDefinitionEnvMapper.selectByExample(example);

        if (CollectionUtils.isNotEmpty(list)) {
            ApiTestEnvironmentExample environmentExample = new ApiTestEnvironmentExample();
            if (StringUtils.isNotEmpty(list.get(0).getEnvId())) {
                environmentExample.createCriteria().andProjectIdEqualTo(projectId).andIdEqualTo(list.get(0).getEnvId());
                long count = apiTestEnvironmentMapper.countByExample(environmentExample);
                if (count > 0) {
                    return list.get(0);
                }
            }
        }
        return null;
    }

    public ApiDefinitionEnv get(String userId) {
        ApiDefinitionEnvExample example = new ApiDefinitionEnvExample();
        example.createCriteria().andUserIdEqualTo(userId);
        List<ApiDefinitionEnv> list = apiDefinitionEnvMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }
}
