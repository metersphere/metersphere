package io.metersphere.service.definition;

import io.metersphere.api.dto.ApiReportEnvConfigDTO;
import io.metersphere.api.dto.MsgDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionEnvMapper;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.RequestResult;
import io.metersphere.service.BaseTestResourcePoolService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionEnvService {
    @Resource
    private ApiDefinitionEnvMapper apiDefinitionEnvMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

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

    public void setEnvAndPoolName(RequestResult baseResult, RequestResultExpandDTO expandDTO) {
        if (StringUtils.isNotBlank(baseResult.getThreadName())) {
            ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(baseResult.getThreadName());
            if (result != null && StringUtils.isNotEmpty(result.getEnvConfig())) {
                ApiReportEnvConfigDTO envConfig = apiDefinitionService.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig());
                if (envConfig != null) {
                    expandDTO.setEnvName(envConfig.getEnvName());
                    expandDTO.setPoolName(envConfig.getResourcePoolName());
                }
            }
        }
    }

    public void setEnvAndPoolName(MsgDTO dto) {
        if (StringUtils.isNotBlank(dto.getToReport())) {
            ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(dto.getToReport());
            if (result != null && StringUtils.isNotEmpty(result.getEnvConfig())) {
                if (StringUtils.equals("null",result.getEnvConfig())) {
                    if (StringUtils.isNotBlank(result.getActuator())) {
                        Map map = JSON.parseObject(dto.getContent().substring(7), Map.class);
                        TestResourcePool resourcePool = baseTestResourcePoolService.getResourcePool(result.getActuator());
                        map.put("poolName", resourcePool.getName());
                        dto.setContent("result_" + JSON.toJSONString(map));
                    }
                } else {
                    ApiReportEnvConfigDTO envConfig = apiDefinitionService.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig());
                    if (envConfig != null) {
                        Map map = JSON.parseObject(dto.getContent().substring(7), Map.class);
                        map.put("envName", envConfig.getEnvName());
                        map.put("poolName", envConfig.getResourcePoolName());
                        dto.setContent("result_" + JSON.toJSONString(map));
                    }
                }
            }
        }
    }
}
