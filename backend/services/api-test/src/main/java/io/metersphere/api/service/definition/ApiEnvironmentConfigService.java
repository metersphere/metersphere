package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiEnvironmentConfig;
import io.metersphere.api.domain.ApiEnvironmentConfigExample;
import io.metersphere.api.mapper.ApiEnvironmentConfigMapper;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class)
public class ApiEnvironmentConfigService {
    @Resource
    private ApiEnvironmentConfigMapper apiEnvironmentConfigMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ApiEnvironmentConfigLogService apiEnvironmentConfigLogService;


    public ApiEnvironmentConfig get(String userId, String projectId) {
        ApiEnvironmentConfigExample example = new ApiEnvironmentConfigExample();
        example.createCriteria().andCreateUserEqualTo(userId);
        List<ApiEnvironmentConfig> list = apiEnvironmentConfigMapper.selectByExample(example);

        if (CollectionUtils.isNotEmpty(list)) {
            EnvironmentExample environmentExample = new EnvironmentExample();
            if (StringUtils.isNotEmpty(list.get(0).getEnvironmentId())) {
                environmentExample.createCriteria().andProjectIdEqualTo(projectId).andIdEqualTo(list.get(0).getEnvironmentId());
                long count = environmentMapper.countByExample(environmentExample);
                if (count > 0) {
                    return list.get(0);
                }
            }
        }
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andProjectIdEqualTo(projectId).andMockEqualTo(true);
        List<Environment> environments = environmentMapper.selectByExample(environmentExample);
        list.get(0).setEnvironmentId(environments.get(0).getId());
        return list.get(0);
    }

    public String add(String envId, String userId, String projectId) {
        ApiEnvironmentConfig env = new ApiEnvironmentConfig();
        env.setId(IDGenerator.nextStr());
        env.setCreateTime(System.currentTimeMillis());
        env.setUpdateTime(System.currentTimeMillis());
        env.setEnvironmentId(envId);
        env.setCreateUser(userId);
        ApiEnvironmentConfig apiDefinitionEnv = this.get(userId);
        if (apiDefinitionEnv == null) {
            apiEnvironmentConfigMapper.insert(env);
        } else {
            apiDefinitionEnv.setEnvironmentId(envId);
            apiDefinitionEnv.setUpdateTime(System.currentTimeMillis());
            apiEnvironmentConfigMapper.updateByPrimaryKey(apiDefinitionEnv);
            return apiDefinitionEnv.getId();
        }
        apiEnvironmentConfigLogService.addLog(env, projectId);
        return env.getId();
    }

    public ApiEnvironmentConfig get(String userId) {
        ApiEnvironmentConfigExample example = new ApiEnvironmentConfigExample();
        example.createCriteria().andCreateUserEqualTo(userId);
        List<ApiEnvironmentConfig> list = apiEnvironmentConfigMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

}
