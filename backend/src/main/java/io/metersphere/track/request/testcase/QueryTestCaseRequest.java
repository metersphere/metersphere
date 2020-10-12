package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestCaseRequest extends TestCase {

    private String name;

    private List<String> nodeIds;

    private List<String> testCaseIds;

    private List<OrderRequest> orders;

    private Map<String, List<String>> filters;

    private String planId;

    private String workspaceId;

    private String userId;

    private Map<String, Object> combine;

    private String reviewId;
}
