package io.metersphere.api.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

@Data
public class ApiScenarioReportDTO {

    private List<StepTreeDTO> steps;

    private String console;
    private long totalTime;
    private long total;
    private long error;
    private long errorCode;
    private long unExecute;

    private long scenarioTotal;
    private long scenarioError;
    private long scenarioSuccess;
    private long scenarioErrorReport;
    private long scenarioUnExecute;

    private long scenarioStepTotal;
    private long scenarioStepError;
    private long scenarioStepSuccess;
    private long scenarioStepErrorReport;
    private long scenarioStepPending;


    private long totalAssertions = 0;
    private long passAssertions = 0;
    private long errorCodeAssertions = 0;

    private String actuator;
    private String name;
    private String envConfig;

    //<项目名称，<环境名称>>
    private LinkedHashMap<String, List<String>> projectEnvMap;

    private String mode;
    private String poolName;

}
