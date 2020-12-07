package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiScenarioReport;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIScenarioReportResult extends ApiScenarioReport {

    private String testName;

    private String projectName;

    private String userName;

    private String content;
}
