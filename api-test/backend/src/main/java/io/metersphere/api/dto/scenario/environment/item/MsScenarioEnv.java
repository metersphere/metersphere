package io.metersphere.api.dto.scenario.environment.item;

import lombok.Data;

import java.util.Map;

@Data
public class MsScenarioEnv {
    private String environmentId;
    private Map<String, String> environmentMap;
}
