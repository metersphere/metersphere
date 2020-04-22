package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SaveAPITestRequest {

    private String id;

    private String projectId;

    private String name;

    private String scenarioDefinition;
}
