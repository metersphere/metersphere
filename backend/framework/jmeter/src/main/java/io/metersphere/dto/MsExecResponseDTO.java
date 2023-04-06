package io.metersphere.dto;

import lombok.Data;

@Data
public class MsExecResponseDTO {

    private String testId;

    private String reportId;

    private String runMode;

    public MsExecResponseDTO() {

    }

    public MsExecResponseDTO(String testId, String reportId, String runMode) {
        this.testId = testId;
        this.reportId = reportId;
        this.runMode = runMode;
    }
}
