package io.metersphere.track.request.testplan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunTestPlanRequest extends TestPlanRequest {
    private String userId;
    private String triggerMode;
    private String testPlanLoadId;
}
