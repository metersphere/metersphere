package io.metersphere.plan.request.performance;


import io.metersphere.dto.TestPlanLoadCaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoadPlanReportDTO {
    private List<TestPlanLoadCaseDTO> loadAllCases;
    private List<TestPlanLoadCaseDTO> loadFailureCases;
}
