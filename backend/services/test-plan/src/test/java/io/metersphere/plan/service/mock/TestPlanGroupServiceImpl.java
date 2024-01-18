package io.metersphere.plan.service.mock;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.service.TestPlanGroupService;
import org.springframework.stereotype.Service;

@Service
public class TestPlanGroupServiceImpl implements TestPlanGroupService {

    @Override
    public boolean validateGroup(TestPlan testPlan, TestPlanConfig testPlanConfig) {

        return true;
    }

}
