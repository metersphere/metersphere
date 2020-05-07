package io.metersphere.controller.request.testplan;

import lombok.Data;

@Data
public class TestPlanRequest {

    private String id;

    private String projectId;

    private String name;

    private String description;

    private String scenarioDefinition;

    private Long createTime;

    private Long updateTime;

    private String loadConfiguration;

    private String advancedConfiguration;

    private String runtimeConfiguration;

    private String schedule;

    private String testResourcePoolId;

    private static final long serialVersionUID = 1L;

}
