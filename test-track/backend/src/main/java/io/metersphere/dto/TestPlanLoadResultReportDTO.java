package io.metersphere.dto;

import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanLoadResultReportDTO {
    private List<TestCaseReportStatusResultDTO> caseData;
}

