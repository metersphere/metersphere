package io.metersphere.api.dto;

import io.metersphere.api.dto.automation.RunModeConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunRequest {
    private String testId;
    private String userId;
    private boolean isDebug;
    private String runMode;
    private String jmx;
    private RunModeConfig config;
}
