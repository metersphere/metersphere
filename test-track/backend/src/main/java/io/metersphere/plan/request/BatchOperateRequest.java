package io.metersphere.plan.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchOperateRequest {
    private List<String> ids;
    boolean selectAll;
    private List<String> unSelectIds;
    private QueryTestPlanRequest queryTestPlanRequest;
    private String projectId;
}
