package io.metersphere.quota.dto;

import lombok.Data;

@Data
public class ReportTimeInfo {
    private long duration;
    private long startTime;
    private long endTime;
}
