package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanLoadCaseExample;
import io.metersphere.plan.mapper.TestPlanLoadCaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;

    public void deleteByTestPlanId(String testPlanId) {
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        testPlanLoadCaseMapper.deleteByExample(example);
    }

    public void deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        testPlanLoadCaseMapper.deleteByExample(example);
    }
}
