package io.metersphere.service.remote.ui;

import io.metersphere.base.domain.UiScenario;
import io.metersphere.service.remote.project.TrackUiTestService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelevanceUiCaseService extends TrackUiTestService {

    private static final String BASE_URL = "/test/case";

    public List<UiScenario> getUiCaseByIds(List<String> ids) {
        return microService.postForDataArray(serviceName, BASE_URL + "/getUiCaseByIds", ids, UiScenario.class);
    }
}
