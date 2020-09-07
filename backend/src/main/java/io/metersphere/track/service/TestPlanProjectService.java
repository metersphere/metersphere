package io.metersphere.track.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.base.domain.TestPlanProject;
import io.metersphere.base.domain.TestPlanProjectExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanProjectService {
    
    @Resource
    TestPlanProjectMapper testPlanProjectMapper;
    @Resource
    ProjectMapper projectMapper;
    
    public List<String> getProjectIdsByPlanId(String planId) {
        TestPlanProjectExample example = new TestPlanProjectExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        List<String> projectIds = testPlanProjectMapper.selectByExample(example)
                .stream()
                .map(TestPlanProject::getProjectId)
                .collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return null;
        }

        return projectIds;
    }

    public List<Project> getProjectByPlanId(String planId) {
        List<String> projectIds = getProjectIdsByPlanId(planId);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);
        List<Project> projects = projectMapper.selectByExample(projectExample);
        return Optional.ofNullable(projects).orElse(new ArrayList<>());
    }
}
