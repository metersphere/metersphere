package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ApiScenarioEnvRequest {

    private String definition;
    private String environmentType;
    private String environmentGroupId;
    private Map<String, String> environmentMap;
    private Boolean environmentEnable = false;
    private String id;
}
