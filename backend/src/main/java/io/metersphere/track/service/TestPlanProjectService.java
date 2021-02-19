package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanProjectMapper;
import io.metersphere.track.request.testplancase.TestCaseRelevanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanProjectService {

    @Resource
    TestPlanProjectMapper testPlanProjectMapper;
    @Resource
    ProjectMapper projectMapper;
    @Resource
    private TestPlanMapper testPlanMapper;

    public List<String> getProjectIdsByPlanId(String planId) {
        TestPlan plan = testPlanMapper.selectByPrimaryKey(planId);
        String workspaceId = plan.getWorkspaceId();
        if (StringUtils.isNotBlank(workspaceId)) {
            ProjectExample example = new ProjectExample();
            example.createCriteria().andWorkspaceIdEqualTo(workspaceId);
            List<Project> projects = projectMapper.selectByExample(example);
            return projects.stream().map(Project::getId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<Project> getProjectByPlanId(TestCaseRelevanceRequest request) {
        ProjectExample projectExample = new ProjectExample();
        ProjectExample.Criteria criteria = projectExample.createCriteria();
        criteria.andIdIn(request.getProjectIds());
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        return projectMapper.selectByExample(projectExample);
    }

    public void deleteTestPlanProjectByPlanId(String planId) {
        TestPlanProjectExample testPlanProjectExample = new TestPlanProjectExample();
        testPlanProjectExample.createCriteria().andTestPlanIdEqualTo(planId);
        testPlanProjectMapper.deleteByExample(testPlanProjectExample);
    }

    public List<String> getPlanIdByProjectId(String projectId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);

        if (CollectionUtils.isEmpty(testPlans)) {
            return null;
        }
        return testPlans
                .stream()
                .map(TestPlan::getId)
                .collect(Collectors.toList());
    }
}
