package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanReportExecuteCheckResultDTO {
    private boolean isTimeOut;
    private boolean isFinishedCaseChanged;
}
