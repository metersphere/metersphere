package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestPlan;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlan {

    private boolean recent = false;
}
