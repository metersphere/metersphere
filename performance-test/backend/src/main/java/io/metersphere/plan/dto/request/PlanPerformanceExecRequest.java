package io.metersphere.plan.dto.request;


import io.metersphere.dto.RunModeConfigDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class PlanPerformanceExecRequest {
    private String planReportId;
    private RunModeConfigDTO config;
    private String triggerMode;
    private Map<String, String> perfMap;
}
