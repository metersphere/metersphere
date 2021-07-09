package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiScenarioReport implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String userId;

    private String triggerMode;

    private String executeType;

    private String scenarioName;

    private String scenarioId;

    private String createUser;

    private String actuator;

    private String description;

    private String testPlanScenarioId;

    private static final long serialVersionUID = 1L;
}
