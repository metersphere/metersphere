package io.metersphere.dto;

import io.metersphere.base.domain.UiScenarioReportWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ScenarioReportResultWrapper extends UiScenarioReportWithBLOBs {

    private String testName;

    private String projectName;

    private String testId;

    private String userName;

    private List<String> scenarioIds;

    private String content;
}
