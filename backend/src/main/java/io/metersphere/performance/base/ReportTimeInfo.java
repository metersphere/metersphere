package io.metersphere.performance.base;

import lombok.Data;

@Data
public class ReportTimeInfo {
    private long duration;
    private long startTime;
    private long endTime;
}
