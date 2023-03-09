package io.metersphere.plan.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AutomationsRunInfoDTO {
    List<String> resourcePools;
    Map<String, List<String>> projectEnvMap;
}
