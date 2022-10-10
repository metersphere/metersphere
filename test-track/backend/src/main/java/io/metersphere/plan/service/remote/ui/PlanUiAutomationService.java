package io.metersphere.plan.service.remote.ui;

import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.plan.reuest.api.RunScenarioRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanUiAutomationService extends UiTestService {

    private static final String BASE_UEL = "/ui/automation";

    public List<MsExecResponseDTO> run(RunScenarioRequest request) {
        return microService.postForDataArray(serviceName, BASE_UEL + "/plan/run", request, MsExecResponseDTO.class);
    }
}
