package io.metersphere.plan.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ApiReportResultDTO {
    // <reportId status>接口报告结果
    Map<String, String> apiReportResultMap;
    
    // <reportId status>场景报告结果
    Map<String, String> scenarioReportResultMap;
}
