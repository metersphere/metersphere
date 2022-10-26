package io.metersphere.plan.service.remote.api;

import io.metersphere.plan.request.api.ApiDefinitionRequest;
import io.metersphere.plan.service.TestPlanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PlanApiDefinitionService extends ApiTestService {

    private static final String BASE_UEL = "/api/definition";

    @Resource
    @Lazy
    TestPlanService testPlanService;

    public Object listRelevance(ApiDefinitionRequest request, int pageNum, int pageSize) {
        if (StringUtils.isNotBlank(request.getPlanId()) && testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setRepeatCase(true);
        }
        return microService.postForData(serviceName, BASE_UEL + String.format("/list/relevance/%s/%s", pageNum, pageSize), request);
    }
}
