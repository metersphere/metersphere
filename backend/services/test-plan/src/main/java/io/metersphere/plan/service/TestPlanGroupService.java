package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;

public interface TestPlanGroupService {
    boolean validateGroup(TestPlan testPlan, TestPlanConfig testPlanConfig);
}
