package io.metersphere.plan.dto;

import io.metersphere.base.domain.ApiScenarioReportWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class APIScenarioReportResult extends ApiScenarioReportWithBLOBs {

    private String testName;

    private String projectName;

    private String testId;

    private String userName;

    private List<String> scenarioIds;

    private String content;
}
