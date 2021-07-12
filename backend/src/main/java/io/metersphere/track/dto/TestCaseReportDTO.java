package io.metersphere.track.dto;

import io.metersphere.base.domain.TestCaseReport;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseReportDTO extends TestCaseReport {
    private String createName;
}
