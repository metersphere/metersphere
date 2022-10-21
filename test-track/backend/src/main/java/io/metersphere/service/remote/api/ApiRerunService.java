package io.metersphere.service.remote.api;

import io.metersphere.dto.TestPlanRerunParametersDTO;
import org.springframework.stereotype.Service;

@Service
public class ApiRerunService extends TrackApiTestService {

    private static final String BASE_URL = "/api/test/exec/rerun/test/plan";

    public String rerun(TestPlanRerunParametersDTO paramDTO) {
        return microService.postForData(serviceName, BASE_URL, paramDTO, String.class);
    }
}
