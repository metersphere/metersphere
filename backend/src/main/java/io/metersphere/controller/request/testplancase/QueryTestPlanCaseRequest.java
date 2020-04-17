package io.metersphere.controller.request.testplancase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestPlanTestCase;

import java.util.List;

public class QueryTestPlanCaseRequest extends TestPlanTestCase {

    private List<Integer> nodeIds;

    private String workspaceId;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
