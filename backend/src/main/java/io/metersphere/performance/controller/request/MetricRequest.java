package io.metersphere.performance.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MetricRequest {
    private List<MetricDataRequest> metricDataQueries = new ArrayList<>();
    private long startTime;
    private long endTime;
    private int step = 15;
}
