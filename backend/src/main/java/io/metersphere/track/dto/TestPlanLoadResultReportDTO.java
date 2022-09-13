package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TestPlanLoadResultReportDTO implements Serializable {
    private List<TestCaseReportStatusResultDTO> caseData;
}

