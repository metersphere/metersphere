package io.metersphere.service;

import io.metersphere.track.request.testplan.TestPlanRequest;

import java.util.Set;

public interface QuotaService {

    void checkAPITestQuota();

    void checkLoadTestQuota(TestPlanRequest request, boolean checkPerformance);

    Set<String> getQuotaResourcePools();
}
