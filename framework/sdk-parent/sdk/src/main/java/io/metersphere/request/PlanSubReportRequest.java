package io.metersphere.request;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class PlanSubReportRequest {
    private Map config;
    private String planId;
    private Boolean saveResponse;
    private Map<String, String> reportIdMap;
}
