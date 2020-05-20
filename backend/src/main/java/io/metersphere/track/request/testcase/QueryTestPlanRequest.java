package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestPlan;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlan {

    private boolean recent = false;
    private List<String> planIds;
}
