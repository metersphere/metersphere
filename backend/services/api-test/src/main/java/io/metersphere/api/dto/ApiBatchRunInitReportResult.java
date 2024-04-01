package io.metersphere.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-04-01  11:29
 */
@Data
public class ApiBatchRunInitReportResult {
    private Map<String, String> scenarioReportMap = new HashMap<>();
    private Long requestCount = 0L;
    private Map<String, Long> scenarioCountMap = new HashMap<>();
}
