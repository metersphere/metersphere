package io.metersphere.provider;

import java.util.List;

public interface BaseTestPlanProvider {
    List<String> selectTestPlanIdByFunctionCaseAndStatus(String caseId, List<String> statusList);
}
