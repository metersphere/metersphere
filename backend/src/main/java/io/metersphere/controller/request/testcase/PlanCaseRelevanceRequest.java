package io.metersphere.controller.request.testcase;

import java.util.ArrayList;
import java.util.List;

public class PlanCaseRelevanceRequest {

    private String planId;
    private List<String> testCaseIds = new ArrayList<>();

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public List<String> getTestCaseIds() {
        return testCaseIds;
    }

    public void setTestCaseIds(List<String> testCaseIds) {
        this.testCaseIds = testCaseIds;
    }
}
