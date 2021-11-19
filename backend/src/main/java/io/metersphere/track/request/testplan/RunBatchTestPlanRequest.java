package io.metersphere.track.request.testplan;

import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.performance.request.RunTestPlanRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RunBatchTestPlanRequest  {
    private List<RunTestPlanRequest> requests;
    private RunModeConfigDTO config;
    private String userId;
}
