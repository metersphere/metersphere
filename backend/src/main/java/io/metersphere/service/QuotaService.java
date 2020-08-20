package io.metersphere.service;

import io.metersphere.track.request.testplan.SaveTestPlanRequest;

public interface QuotaService {

    void checkAPITestQuota();

    void checkLoadTestQuota(SaveTestPlanRequest request);
}
