package io.metersphere.plan.service.remote.performance;

import io.metersphere.base.domain.LoadTest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class PlanPerformanceTestService extends LoadTestService {

    private static final String BASE_UEL = "/performance";

    public LoadTest get(String id) {
        return microService.getForData(serviceName, BASE_UEL + "/get/" + id, LoadTest.class);
    }
}
