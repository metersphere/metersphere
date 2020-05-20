package io.metersphere.track.request.testplan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlanRequest {
    private String workspaceId;
}
