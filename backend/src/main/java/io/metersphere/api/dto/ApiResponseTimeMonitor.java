package io.metersphere.api.dto;

import lombok.Data;

@Data
public class ApiResponseTimeMonitor {

    private String id;

    private String reportId;

    private String url;

    private String apiName;

    private String startTime;

    private String responseTime;

}
