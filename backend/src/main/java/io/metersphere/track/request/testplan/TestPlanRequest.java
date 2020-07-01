package io.metersphere.track.request.testplan;

import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    private Schedule schedule;

    private String testResourcePoolId;

    private static final long serialVersionUID = 1L;

}
