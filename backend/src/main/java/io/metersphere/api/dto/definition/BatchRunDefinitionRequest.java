package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.automation.RunModeConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BatchRunDefinitionRequest {
    private String id;

    private List<String> planIds;

    private RunModeConfig config;

}
