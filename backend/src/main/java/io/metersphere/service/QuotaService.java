package io.metersphere.service;


import io.metersphere.performance.request.TestPlanRequest;

import java.util.Set;

public interface QuotaService {

    void checkAPIDefinitionQuota();

    void checkAPIAutomationQuota();

    void checkLoadTestQuota(TestPlanRequest request, boolean checkPerformance);

    Set<String> getQuotaResourcePools();
}
