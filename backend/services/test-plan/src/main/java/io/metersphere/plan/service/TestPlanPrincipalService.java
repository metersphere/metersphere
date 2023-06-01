package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanPrincipal;
import io.metersphere.plan.mapper.TestPlanPrincipalMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanPrincipalService {

    @Resource
    TestPlanPrincipalMapper testPlanPrincipalMapper;

    public void batchSave(@NotEmpty List<TestPlanPrincipal> testPlanPrincipalList) {
        for (TestPlanPrincipal testPlanPrincipal : testPlanPrincipalList) {
            testPlanPrincipalMapper.insert(testPlanPrincipal);
        }
    }
}
