package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RunScenarioRequest {

    private String id;

    private String reportId;

    private String projectId;

    private String environmentId;

    private String triggerMode;

    private String executeType;

    private String runMode;

    private List<String> planCaseIds;

    private String reportUserID;

    private List<String> scenarioIds;
}
