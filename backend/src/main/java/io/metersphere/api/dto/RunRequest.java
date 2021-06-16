package io.metersphere.api.dto;

import io.metersphere.api.dto.automation.RunModeConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunRequest {
    private String testId;
    private String poolId;
    // api / case 或有这个属性值
    private String reportId;
    private String url;
    private String userId;
    private boolean isDebug;
    private String runMode;
    private String jmx;
    private RunModeConfig config;
}
