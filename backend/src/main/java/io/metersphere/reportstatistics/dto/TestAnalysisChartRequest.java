package io.metersphere.reportstatistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestAnalysisChartRequest {
    private boolean createCase;
    private boolean updateCase;
    private String order;
    private List<Long> times;
    private String startTime;
    private String endTime;
    private List<String> prioritys;
    private List<String> projects;
    private List<String> modules;
    private List<String> users;
}
