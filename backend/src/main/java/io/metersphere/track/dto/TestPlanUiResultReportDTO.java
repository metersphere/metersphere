package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanUiResultReportDTO {
    private List<TestCaseReportStatusResultDTO> uiScenarioCaseData;
    private List<TestCaseReportStatusResultDTO> uiScenarioData;
    private List<TestCaseReportStatusResultDTO> uiScenarioStepData;
}

