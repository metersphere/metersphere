package io.metersphere.api.dto;

import io.metersphere.api.dto.automation.RunModeConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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
    // 集成报告ID
    private String amassReport;
    private RunModeConfig config;

    private Map<String, Object> kafka;
}
