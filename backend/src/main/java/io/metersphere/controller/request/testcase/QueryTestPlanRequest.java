package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestPlan;

import java.util.List;

public class QueryTestPlanRequest extends TestPlan {

    private boolean recent = false;

    public boolean isRecent() {
        return recent;
    }

    public void setRecent(boolean recent) {
        this.recent = recent;
    }
}
