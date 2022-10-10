package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.EnvironmentGroupProjectMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.BaseEnvGroupProjectMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.environment.dto.EnvironmentGroupProjectDTO;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentGroupProjectService {

    @Resource
    private BaseEnvGroupProjectMapper baseEnvGroupProjectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private EnvironmentGroupProjectMapper environmentGroupProjectMapper;
    @Resource
    private BaseProjectService baseProjectService;

    public List<EnvironmentGroupProjectDTO> getList(String groupId) {
        List<EnvironmentGroupProjectDTO> list = baseEnvGroupProjectMapper.getList(groupId);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(e -> {
                String projectId = e.getProjectId();
                ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
                example.createCriteria().andProjectIdEqualTo(projectId);
                List<ApiTestEnvironmentWithBLOBs> environments = apiTestEnvironmentMapper.selectByExampleWithBLOBs(example);
                e.setEnvironments(environments);
            });
        }
        return list;
    }

    public String getEnvJson(String groupId) {
        List<EnvironmentGroupProjectDTO> list = baseEnvGroupProjectMapper.getList(groupId);
        Map<String, String> map = list.stream()
                .collect(Collectors.toMap(EnvironmentGroupProject::getProjectId, EnvironmentGroupProject::getEnvironmentId));
        return JSON.toJSONString(map);
    }

    public Map<String, String> getEnvMap(String groupId) {
        List<EnvironmentGroupProjectDTO> list = baseEnvGroupProjectMapper.getList(groupId);
        return list.stream()
                .collect(Collectors.toMap(EnvironmentGroupProject::getProjectId, EnvironmentGroupProject::getEnvironmentId));
    }

    public void deleteRelateEnv(String environmentId) {
        if (StringUtil.isBlank(environmentId)) {
            return;
        }
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(environmentId);
        environmentGroupProjectMapper.deleteByExample(example);
    }

    public void deleteRelateProject(String projectId) {
        if (StringUtil.isBlank(projectId)) {
            return;
        }
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        environmentGroupProjectMapper.deleteByExample(example);
    }

    public Map<String, String> getEnvNameMap(String groupId) {
        Map<String, String> map = this.getEnvMap(groupId);
        Set<String> set = map.keySet();
        HashMap<String, String> envMap = new HashMap<>(16);
        if (set.isEmpty()) {
            return envMap;
        }
        for (String projectId : set) {
            String envId = map.get(projectId);
            if (StringUtils.isBlank(envId)) {
                continue;
            }
            Project project = baseProjectService.getProjectById(projectId);
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentMapper.selectByPrimaryKey(envId);
            if (project == null || environment == null) {
                continue;
            }
            String projectName = project.getName();
            String envName = environment.getName();
            if (StringUtils.isBlank(projectName) || StringUtils.isBlank(envName)) {
                continue;
            }
            envMap.put(projectName, envName);
        }
        return envMap;
    }

}
