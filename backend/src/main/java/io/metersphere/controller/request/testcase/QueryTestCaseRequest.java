package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestCaseRequest extends TestCase {

    private List<String> nodeIds;

    private List<OrderRequest> orders;

    private Map<String, List<String>> filters;

    private String planId;

    private String workspaceId;
}
