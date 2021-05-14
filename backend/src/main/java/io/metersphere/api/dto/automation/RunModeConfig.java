package io.metersphere.api.dto.automation;

import lombok.Data;

@Data
public class RunModeConfig {
    private String mode;
    private String reportType;
    private String reportName;
    private String reportId;
    private boolean onSampleError;
    private String resourcePoolId;
}
