package io.metersphere.track.request.testplan;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class LoadCaseRequest extends TestPlanLoadCase {
    private List<String> ids;
    private String projectId;
    private List<String> caseIds;
    private String name;
    private String status;
    private Map<String, List<String>> filters;
    private List<OrderRequest> orders;
    private Map<String, Object> combine;
    private String versionId;
    private String refId;
    // 测试计划是否允许重复
    private boolean repeatCase;

    private List<String> notInIds;
}
