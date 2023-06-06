package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanFunctionCaseExample;
import io.metersphere.plan.mapper.TestPlanFunctionCaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFunctionCaseService {
    @Resource
    private TestPlanFunctionCaseMapper testPlanFunctionCaseMapper;

    public int deleteByTestPlanId(String testPlanId) {
        TestPlanFunctionCaseExample example = new TestPlanFunctionCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        return testPlanFunctionCaseMapper.deleteByExample(example);
    }

    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanFunctionCaseExample example = new TestPlanFunctionCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanFunctionCaseMapper.deleteByExample(example);
    }
}
