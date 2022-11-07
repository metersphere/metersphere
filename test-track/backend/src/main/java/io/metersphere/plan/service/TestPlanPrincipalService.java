package io.metersphere.plan.service;

import io.metersphere.base.domain.TestPlanPrincipal;
import io.metersphere.base.domain.TestPlanPrincipalExample;
import io.metersphere.base.mapper.TestPlanPrincipalMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanPrincipalService {

    @Resource
    private TestPlanPrincipalMapper testPlanPrincipalMapper;


    public void deleteTestPlanPrincipalByPlanId(String planId) {
        if (StringUtils.isBlank(planId)) {
            return;
        }
        TestPlanPrincipalExample example = new TestPlanPrincipalExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        testPlanPrincipalMapper.deleteByExample(example);
    }

    public void deletePlanPrincipalByPlanIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }
        TestPlanPrincipalExample example = new TestPlanPrincipalExample();
        example.createCriteria().andTestPlanIdIn(planIds);
        testPlanPrincipalMapper.deleteByExample(example);
    }

    public TestPlanPrincipal insert(TestPlanPrincipal testPlanPrincipal) {
        testPlanPrincipalMapper.insert(testPlanPrincipal);
        return testPlanPrincipal;
    }
}
