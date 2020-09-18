package io.metersphere.track.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.TestCaseReviewProjectMapper;
import io.metersphere.track.request.testreview.TestReviewRelevanceRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestReviewProjectService {

    @Resource
    ProjectMapper projectMapper;
    @Resource
    TestCaseReviewProjectMapper testCaseReviewProjectMapper;

    public List<String> getProjectIdsByPlanId(String reviewId) {
        TestCaseReviewProjectExample example = new TestCaseReviewProjectExample();
        example.createCriteria().andReviewIdEqualTo(reviewId);
        List<String> projectIds = testCaseReviewProjectMapper.selectByExample(example)
                .stream()
                .map(TestCaseReviewProject::getProjectId)
                .collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return null;
        }

        return projectIds;
    }


    public List<Project> getProject(TestReviewRelevanceRequest request) {
        ProjectExample projectExample = new ProjectExample();
        ProjectExample.Criteria criteria = projectExample.createCriteria();
        criteria.andIdIn(request.getProjectIds());
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        return projectMapper.selectByExample(projectExample);
    }
}
