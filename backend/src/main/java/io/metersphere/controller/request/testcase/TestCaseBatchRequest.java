package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.domain.TestPlanTestCase;

import java.util.List;

public class TestCaseBatchRequest extends TestCaseWithBLOBs {

    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
