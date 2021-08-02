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
    private boolean performanceIsExecuting;

//    private String apiCaseIdListJSON;
//    private String scenarioIdListJSON;
//    private String performanceIdListJSON;
    Map<String,String> apiCaseIdMap;
    Map<String,String> scenarioIdMap;
    Map<String,String> performanceIdMap;

    public TestPlanReportSaveRequest(String reportID, String planId, String userId, String triggerMode) {
        this.reportID = reportID;
        this.planId = planId;
        this.userId = userId;
        this.triggerMode = triggerMode;

        this.countResources = true;
    }

    public TestPlanReportSaveRequest(String reportID, String planId, String userId, String triggerMode, boolean apiCaseIsExecuting, boolean scenarioIsExecuting, boolean performanceIsExecuting,
                                     Map<String,String> apiCaseIdMap, Map<String,String> scenarioIdMap, Map<String,String> performanceIdMap) {
        this.reportID = reportID;
        this.planId = planId;
        this.userId = userId;
        this.triggerMode = triggerMode;

        this.countResources = false;

        this.apiCaseIsExecuting = apiCaseIsExecuting;
        this.scenarioIsExecuting = scenarioIsExecuting;
        this.performanceIsExecuting = performanceIsExecuting;

        this.apiCaseIdMap = apiCaseIdMap;
        this.scenarioIdMap = scenarioIdMap;
        this.performanceIdMap = performanceIdMap;
    }
}
