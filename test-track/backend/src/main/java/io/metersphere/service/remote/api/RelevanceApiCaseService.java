package io.metersphere.service.remote.api;

import io.metersphere.base.domain.ApiScenario;
import io.metersphere.base.domain.ApiTestCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelevanceApiCaseService extends TrackApiTestService{

    private static final String BASE_URL = "/api/testcase";

    public List<ApiTestCase> getApiCaseByIds(List<String> ids) {
        return microService.postForDataArray(serviceName, BASE_URL + "/getApiCaseByIds", ids, ApiTestCase.class);
    }

    public List<ApiScenario> getScenarioCaseByIds(List<String> ids) {
        return microService.postForDataArray(serviceName, BASE_URL + "/getScenarioCaseByIds", ids, ApiScenario.class);
    }
}
