package io.metersphere.api.dto.plan;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestPlanEnvInfoDTO {
    private String runMode;
    private String envGroupName;
    private Map<String, List<String>> projectEnvMap;
}
