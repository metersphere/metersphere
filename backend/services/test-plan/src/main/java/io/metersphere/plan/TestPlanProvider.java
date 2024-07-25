package io.metersphere.plan;

import io.metersphere.plan.service.TestPlanManagementService;
import io.metersphere.provider.BaseTestPlanProvider;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("TEST-PLAN")
public class TestPlanProvider implements BaseTestPlanProvider {

    @Resource
    private TestPlanManagementService testPlanManagementService;

    @Override
    public List<String> selectTestPlanIdByFunctionCaseAndStatus(String caseId, List<String> statusList) {
        return testPlanManagementService.selectTestPlanIdByFuncCaseIdAndStatus(caseId, statusList);
    }
}
