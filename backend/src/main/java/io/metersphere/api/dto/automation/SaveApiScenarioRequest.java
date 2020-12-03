package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SaveApiScenarioRequest {
    private String id;

    private String projectId;

    private String tagId;

    private String userId;

    private String apiScenarioModuleId;

    private String modulePath;

    private String name;

    private String level;

    private String status;

    private String principal;

    private Integer stepTotal;

    private String followPeople;

    private String schedule;

    private String description;

    private String scenarioDefinition;
}
