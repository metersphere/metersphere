package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestPlanTestCase;

import java.util.List;

public class TestPlanCaseBatchRequest extends TestPlanTestCase {

    private List<Integer> ids;

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
