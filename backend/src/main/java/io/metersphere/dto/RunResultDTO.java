package io.metersphere.dto;

import lombok.Data;

@Data
public class RunResultDTO {
    private String reportId;
    private String status;
    private String content;
}
