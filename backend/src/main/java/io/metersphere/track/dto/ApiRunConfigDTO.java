package io.metersphere.track.dto;

import lombok.Data;

@Data
public class ApiRunConfigDTO {
    private String mode;
    private String reportType;
    private String onSampleError;
    private String runWithinResourcePool;
    private String resourcePoolId;
}
