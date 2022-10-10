package io.metersphere.task.dto;

import lombok.Data;

@Data
public class TaskRequestDTO {
    private String type;
    private String reportId;
    private String projectId;
    private String userId;

}
