package io.metersphere.plan.service;

import io.metersphere.base.domain.TestPlanFollow;
import io.metersphere.base.domain.TestPlanFollowExample;
import io.metersphere.base.mapper.TestPlanFollowMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFollowService {

    @Resource
    private TestPlanFollowMapper testPlanFollowMapper;


    public void deleteTestPlanFollowByPlanId(String planId) {
        if (StringUtils.isBlank(planId)) {
            return;
        }
        TestPlanFollowExample example = new TestPlanFollowExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        testPlanFollowMapper.deleteByExample(example);
    }

    public void deletePlanFollowByPlanIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }
        TestPlanFollowExample example = new TestPlanFollowExample();
        example.createCriteria().andTestPlanIdIn(planIds);
        testPlanFollowMapper.deleteByExample(example);
    }

    public TestPlanFollow insert(TestPlanFollow testPlanFollow) {
        testPlanFollowMapper.insert(testPlanFollow);
        return testPlanFollow;
    }
}
