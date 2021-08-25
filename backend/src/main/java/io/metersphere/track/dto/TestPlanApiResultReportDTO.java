package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanApiResultReportDTO {
    private List<TestCaseReportStatusResultDTO> apiCaseData;
    private List<TestCaseReportStatusResultDTO> apiScenarioData;
    private List<TestCaseReportStatusResultDTO> apiScenarioStepData;
}

