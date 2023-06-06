package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanApiScenarioExample;
import io.metersphere.plan.mapper.TestPlanApiScenarioMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiScenarioService {
    @Resource
    private TestPlanApiScenarioMapper testPlanApiScenarioMapper;

    public int deleteByTestPlanId(String testPlanId) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        return testPlanApiScenarioMapper.deleteByExample(example);
    }

    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiScenarioExample example = new TestPlanApiScenarioExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanApiScenarioMapper.deleteByExample(example);
    }
}
