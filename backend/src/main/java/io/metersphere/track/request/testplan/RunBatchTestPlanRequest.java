package io.metersphere.track.request.testplan;

import io.metersphere.api.dto.automation.RunModeConfig;
import io.metersphere.performance.request.RunTestPlanRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RunBatchTestPlanRequest  {
    private List<RunTestPlanRequest> requests;
    private RunModeConfig config;
    private String userId;
}
