package io.metersphere.controller.request.testplancase;

import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.controller.request.OrderRequest;
import lombok.Data;

import java.util.List;

@Data
public class QueryTestPlanCaseRequest extends TestPlanTestCase {

    private List<String> nodeIds;

    private List<OrderRequest> orders;

    private String workspaceId;

    private String name;

}
