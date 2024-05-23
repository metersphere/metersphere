package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanConfigExample;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanConfigService {
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    public void delete(String testPlanId) {
        TestPlanConfigExample example = new TestPlanConfigExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        testPlanConfigMapper.deleteByExample(example);
    }

    public void deleteBatch(List<String> testPlanIdList) {
        TestPlanConfigExample example = new TestPlanConfigExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        testPlanConfigMapper.deleteByExample(example);
    }

    public boolean isRepeatCase(String testPlanId) {
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlanId);
        return testPlanConfig.getRepeatCase();
    }
}
