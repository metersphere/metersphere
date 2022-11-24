package io.metersphere.plan.request;

import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.request.OrderRequest;
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

    // 查询功能用例需要关联的性能测试列表
    private String testCaseId;

    private Boolean allowedRepeatCase = false;
}
