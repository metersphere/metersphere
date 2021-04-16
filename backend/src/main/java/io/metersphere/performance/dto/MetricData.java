package io.metersphere.performance.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MetricData {
    private String uniqueLabel;
    private String seriesName;
    private List<Double> values;
    private List<String> timestamps;
    private String instance;
}
