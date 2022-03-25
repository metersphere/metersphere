package io.metersphere.performance.request;

import io.metersphere.base.domain.Schedule;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanRequest {

    private String id;

    private String projectId;

    private String name;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String loadConfiguration;

    private String advancedConfiguration;

    private String runtimeConfiguration;

    private Schedule schedule;

    private String testResourcePoolId;

    private String refId;

    private String versionId;

    private List<String> follows;

    private static final long serialVersionUID = 1L;

}
