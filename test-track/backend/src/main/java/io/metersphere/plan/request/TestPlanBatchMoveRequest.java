package io.metersphere.plan.request;

import lombok.Data;

import java.util.List;

@Data
public class TestPlanBatchMoveRequest {

    private List<String> ids;
    private QueryTestPlanRequest condition;
    private String projectId;
    private String nodeId;
    private String nodePath;
}
