package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.mapper.TestPlanFollowerMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanFollowerService {

    @Resource
    TestPlanFollowerMapper testPlanFollowerMapper;

    public void batchSave(List<TestPlanFollower> testPlanFollowerList) {
        for (TestPlanFollower testPlanFollower : testPlanFollowerList) {
            testPlanFollowerMapper.insert(testPlanFollower);
        }
    }
}
