package io.metersphere.service;

import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class EnvironmentService {
    @Resource
    private BaseEnvGroupProjectService envGroupProjectService;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private BaseProjectService baseProjectService;

    public Map<String, String> getEnvNameMap(String groupId) {
        Map<String, String> map = envGroupProjectService.getEnvMap(groupId);
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
