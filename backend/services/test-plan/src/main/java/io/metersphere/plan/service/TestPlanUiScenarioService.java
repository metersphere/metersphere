package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanUiScenarioExample;
import io.metersphere.plan.mapper.TestPlanUiScenarioMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanUiScenarioService {
    @Resource
    private TestPlanUiScenarioMapper TestPlanUiScenarioMapper;

    public int deleteByTestPlanId(String testPlanId) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        return TestPlanUiScenarioMapper.deleteByExample(example);
    }

    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanUiScenarioExample example = new TestPlanUiScenarioExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return TestPlanUiScenarioMapper.deleteByExample(example);
    }
}
