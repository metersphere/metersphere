package io.metersphere.dto;

import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseReportAdvanceStatusResultDTO {
    private List<TestCaseReportStatusResultDTO> functionalResult;
    private List<TestCaseReportStatusResultDTO> apiResult;
    private List<TestCaseReportStatusResultDTO> scenarioResult;
    private List<TestCaseReportStatusResultDTO> loadResult;
    private List<String> executedScenarioIds;
}

