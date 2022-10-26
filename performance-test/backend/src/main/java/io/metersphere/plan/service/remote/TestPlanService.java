package io.metersphere.plan.service.remote;

import io.metersphere.base.domain.TestPlan;
import org.springframework.stereotype.Service;

@Service
public class TestPlanService extends TrackService {

    private static final String BASE_UEL = "/test/plan";

    public TestPlan get(String id) {
        return microService.getForData(serviceName, BASE_UEL + "/get/" + id, TestPlan.class);
    }

    public void statusReset(String id) {
        microService.getForData(serviceName, BASE_UEL + "/status/reset/" + id);
    }
}
