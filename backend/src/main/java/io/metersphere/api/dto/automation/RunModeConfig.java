package io.metersphere.api.dto.automation;

import lombok.Data;

import java.util.Map;

@Data
public class RunModeConfig {
    private String mode;
    private String reportType;
    private String reportName;
    private String reportId;
    private boolean onSampleError;
    private String resourcePoolId;

    /**
     * 运行环境
     */
    private Map<String, String> envMap;
}
