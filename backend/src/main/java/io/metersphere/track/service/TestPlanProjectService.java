package io.metersphere.track.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.base.domain.TestPlanProject;
import io.metersphere.base.domain.TestPlanProjectExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestPlanProjectMapper;
import io.metersphere.track.request.testplancase.TestCaseRelevanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
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
        TestPlanProjectExample testPlanProjectExample = new TestPlanProjectExample();
        testPlanProjectExample.createCriteria().andProjectIdEqualTo(projectId);
        List<TestPlanProject> testPlanProjects = testPlanProjectMapper.selectByExample(testPlanProjectExample);
        if (CollectionUtils.isEmpty(testPlanProjects)) {
            return null;
        }
        return testPlanProjects
                .stream()
                .map(TestPlanProject::getTestPlanId)
                .collect(Collectors.toList());
    }
}
