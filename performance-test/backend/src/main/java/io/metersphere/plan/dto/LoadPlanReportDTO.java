package io.metersphere.plan.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoadPlanReportDTO {
    private List<TestPlanLoadCaseDTO> loadAllCases;
    private List<TestPlanLoadCaseDTO> loadFailureCases;
}
