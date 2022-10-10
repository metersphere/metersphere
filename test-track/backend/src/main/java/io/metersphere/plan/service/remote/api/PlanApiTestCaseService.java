package io.metersphere.plan.service.remote.api;

import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class PlanApiTestCaseService  extends ApiTestService {
    private static final String BASE_UEL = "/api/testcase";

    public ApiTestCaseWithBLOBs get(@PathVariable String id) {
        return microService.getForData(serviceName, BASE_UEL + "/get/" + id, ApiTestCaseWithBLOBs.class);
    }

}
