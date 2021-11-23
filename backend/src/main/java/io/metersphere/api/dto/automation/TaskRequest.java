package io.metersphere.api.dto.automation;

import lombok.Data;

@Data
public class TaskRequest {
    private String type;
    private String reportId;
    private String projectId;

}
