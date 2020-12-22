package io.metersphere.api.dto.automation;

import io.metersphere.api.dto.definition.request.MsTestElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaveApiScenarioRequest {
    private String id;

    private String projectId;

    private String tags;

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

    private MsTestElement scenarioDefinition;

    List<String> bodyUploadIds;
}
