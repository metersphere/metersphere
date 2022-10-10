package io.metersphere.service.remote.performance;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.service.remote.project.TrackPerformanceTestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelevanceLoadCaseService extends TrackPerformanceTestService {

    private static final String BASE_URL = "/performance";

    public List<LoadTest> getLoadCaseByIds(List<String> ids) {
        return microService.postForDataArray(serviceName, BASE_URL + "/getLoadCaseByIds", ids, LoadTest.class);
    }
}
