package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ScenarioEnv {

    private Set<String> projectIds = new HashSet<>();
    private Boolean fullUrl = true;
}
