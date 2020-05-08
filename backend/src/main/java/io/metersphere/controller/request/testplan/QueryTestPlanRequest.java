package io.metersphere.controller.request.testplan;

import lombok.Data;

@Data
public class QueryTestPlanRequest extends TestPlanRequest {
    private String workspaceId;
    private boolean recent = false; // 表示查询最近的测试
}
