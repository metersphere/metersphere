package io.metersphere.track.request.report;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/1/8 4:36 下午
 * @Description
 */
@Getter
@Setter
public class TestPlanReportSaveRequest {
    private String reportID;
    private String planId;
    private String userId;
    private String triggerMode;

    private boolean countResources;
    private boolean apiCaseIsExecuting;
    private boolean scenarioIsExecuting;
    private boolean uiScenarioIsExecuting;
    private boolean performanceIsExecuting;

    Map<String, String> apiCaseIdMap;
    Map<String, String> scenarioIdMap;
    Map<String, String> uiScenarioIdMap;
    Map<String, String> performanceIdMap;

    public TestPlanReportSaveRequest(String reportID, String planId, String userId, String triggerMode) {
        this.reportID = reportID;
        this.planId = planId;
        this.userId = userId;
        this.triggerMode = triggerMode;

        this.countResources = true;
    }

    public TestPlanReportSaveRequest(Builder builder) {
        this.reportID = builder.reportID;
        this.planId = builder.planId;
        this.userId = builder.userId;
        this.triggerMode = builder.triggerMode;
        this.countResources = builder.countResources;
        this.apiCaseIsExecuting = builder.apiCaseIsExecuting;
        this.scenarioIsExecuting = builder.scenarioIsExecuting;
        this.uiScenarioIsExecuting = builder.uiScenarioIsExecuting;
        this.performanceIsExecuting = builder.performanceIsExecuting;
        this.apiCaseIdMap = builder.apiCaseIdMap;
        this.scenarioIdMap = builder.scenarioIdMap;
        this.uiScenarioIdMap = builder.uiScenarioIdMap;
        this.performanceIdMap = builder.performanceIdMap;
    }

    public static class Builder {
        private String reportID;
        private String planId;
        private String userId;
        private String triggerMode;

        private boolean countResources;
        private boolean apiCaseIsExecuting;
        private boolean scenarioIsExecuting;
        private boolean uiScenarioIsExecuting;
        private boolean performanceIsExecuting;
        Map<String, String> apiCaseIdMap;
        Map<String, String> scenarioIdMap;
        Map<String, String> uiScenarioIdMap;
        Map<String, String> performanceIdMap;

        public Builder setReportID(String reportID) {
            this.reportID = reportID;
            return this;
        }

        public Builder setPlanId(String planId) {
            this.planId = planId;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setTriggerMode(String triggerMode) {
            this.triggerMode = triggerMode;
            return this;
        }

        public Builder setCountResources(boolean countResources) {
            this.countResources = countResources;
            return this;
        }

        public Builder setApiCaseIsExecuting(boolean apiCaseIsExecuting) {
            this.apiCaseIsExecuting = apiCaseIsExecuting;
            return this;
        }

        public Builder setScenarioIsExecuting(boolean scenarioIsExecuting) {
            this.scenarioIsExecuting = scenarioIsExecuting;
            return this;
        }

        public Builder setUiScenarioIsExecuting(boolean uiScenarioIsExecuting) {
            this.uiScenarioIsExecuting = uiScenarioIsExecuting;
            return this;
        }

        public Builder setPerformanceIsExecuting(boolean performanceIsExecuting) {
            this.performanceIsExecuting = performanceIsExecuting;
            return this;
        }

        public Builder setApiCaseIdMap(Map<String, String> apiCaseIdMap) {
            this.apiCaseIdMap = apiCaseIdMap;
            return this;
        }

        public Builder setScenarioIdMap(Map<String, String> scenarioIdMap) {
            this.scenarioIdMap = scenarioIdMap;
            return this;
        }


        public Builder setPerformanceIdMap(Map<String, String> performanceIdMap) {
            this.performanceIdMap = performanceIdMap;
            return this;
        }

        public Builder setUiScenarioIdMap(Map<String, String> uiScenarioIdMap) {
            this.uiScenarioIdMap = uiScenarioIdMap;
            return this;
        }

        public TestPlanReportSaveRequest build() {
            return new TestPlanReportSaveRequest(this);
        }
    }
}
