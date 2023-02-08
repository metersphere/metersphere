package io.metersphere.api.dto;

import lombok.Data;

@Data
public class BodyFileRequest {
    private String reportId;
    private String testId;
}
