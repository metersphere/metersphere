package io.metersphere.dto;

import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TestPlanFunctionResultReportDTO implements Serializable {
    private List<TestCaseReportStatusResultDTO> caseData;
    private List<TestCaseReportStatusResultDTO> issueData;
}

