package io.metersphere.track.dto;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.TestPlanLoadCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanLoadCaseDTO extends TestPlanLoadCase {
    private String userName;
    private String caseName;
    private String projectName;
    private String caseStatus;
    private String num;
    private String name;
    private ReportDTO response;

    @Getter
    @Setter
    public static class ReportDTO extends LoadTestReportWithBLOBs {
        private long duration;
        private long startTime;
        private long endTime;
    }
}
