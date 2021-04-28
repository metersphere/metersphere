package io.metersphere.track.request.report;

import lombok.Getter;
import lombok.Setter;

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
    private boolean performanceIsExecuting;

    private String apiCaseIdListJSON;
    private String scenarioIdListJSON;
    private String performanceIdListJSON;

    public TestPlanReportSaveRequest(String reportID, String planId, String userId, String triggerMode) {
        this.reportID = reportID;
        this.planId = planId;
        this.userId = userId;
        this.triggerMode = triggerMode;

        this.countResources = true;
    }

    public TestPlanReportSaveRequest(String reportID, String planId, String userId, String triggerMode, boolean apiCaseIsExecuting, boolean scenarioIsExecuting, boolean performanceIsExecuting, String apiCaseIdListJSON, String scenarioIdListJSON, String performanceIdListJSON) {
        this.reportID = reportID;
        this.planId = planId;
        this.userId = userId;
        this.triggerMode = triggerMode;

        this.countResources = false;

        this.apiCaseIsExecuting = apiCaseIsExecuting;
        this.scenarioIsExecuting = scenarioIsExecuting;
        this.performanceIsExecuting = performanceIsExecuting;

        this.apiCaseIdListJSON = apiCaseIdListJSON;
        this.scenarioIdListJSON = scenarioIdListJSON;
        this.performanceIdListJSON = performanceIdListJSON;
    }
}
