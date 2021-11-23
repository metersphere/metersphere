package io.metersphere.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.ApiTestEnvironmentExample;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.EnvironmentGroupProject;
import io.metersphere.base.domain.EnvironmentGroupProjectExample;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.EnvironmentGroupProjectMapper;
import io.metersphere.base.mapper.ext.ExtEnvGroupProjectMapper;
import io.metersphere.dto.EnvironmentGroupProjectDTO;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.jsoup.internal.StringUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentGroupProjectService {

    @Resource
    private ExtEnvGroupProjectMapper extEnvGroupProjectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private EnvironmentGroupProjectMapper environmentGroupProjectMapper;
    @Resource
    @Lazy
    private TestPlanScenarioCaseService testPlanScenarioCaseService;

    public List<EnvironmentGroupProjectDTO> getList(String groupId) {
        List<EnvironmentGroupProjectDTO> list = extEnvGroupProjectMapper.getList(groupId);
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
        List<EnvironmentGroupProjectDTO> list = extEnvGroupProjectMapper.getList(groupId);
        Map<String, String> map = list.stream()
                .collect(Collectors.toMap(EnvironmentGroupProject::getProjectId, EnvironmentGroupProject::getEnvironmentId));
        return JSON.toJSONString(map);
    }

    public Map<String, String> getEnvMap(String groupId) {
        List<EnvironmentGroupProjectDTO> list = extEnvGroupProjectMapper.getList(groupId);
        Map<String, String> map = list.stream()
                .collect(Collectors.toMap(EnvironmentGroupProject::getProjectId, EnvironmentGroupProject::getEnvironmentId));
        return map;
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
        Map<String, String> envMap = this.getEnvMap(groupId);
        return testPlanScenarioCaseService.getScenarioCaseEnv(envMap);
    }
}
