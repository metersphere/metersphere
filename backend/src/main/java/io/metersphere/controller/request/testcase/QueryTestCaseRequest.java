package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;
import lombok.Data;

import java.util.List;

@Data
public class QueryTestCaseRequest extends TestCase {

    private List<String> nodeIds;

    private String planId;

    private String workspaceId;
}
