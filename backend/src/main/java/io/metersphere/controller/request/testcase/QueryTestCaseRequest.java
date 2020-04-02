package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;

import java.util.List;

public class QueryTestCaseRequest extends TestCase {

    List<Integer> nodeIds;

    String planId;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public List<Integer> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }
}
