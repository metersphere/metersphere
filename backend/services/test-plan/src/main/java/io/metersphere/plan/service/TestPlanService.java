package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {

    public boolean add(TestPlan testPlan) {
        return testPlan == null;
    }
}
