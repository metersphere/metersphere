package io.metersphere.controller.request.testplancase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestPlanTestCase;
import lombok.Data;

import java.util.List;

@Data
public class QueryTestPlanCaseRequest extends TestPlanTestCase {

    private List<String> nodeIds;

    private String workspaceId;

    private String name;

}
