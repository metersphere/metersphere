package io.metersphere.plan.request.function;

import io.metersphere.request.testcase.QueryTestCaseRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlanCaseRelevanceRequest {
    /**
     * 测试计划ID
     */
    private String planId;
    private String executor;

    private List<String> ids;

    /**
     * 当选择关联全部用例时把加载条件送到后台，从后台查询
     */
    private QueryTestCaseRequest request;

    /**
     * 具体要关联的用例
     */
    private List<String> testCaseIds = new ArrayList<>();

    /**
     * 是否同步关联功能用例下关联的接口场景性能ui用例
     */
    private Boolean checked;
}
