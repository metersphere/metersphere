package io.metersphere.reportstatistics.dto;

import io.metersphere.reportstatistics.dto.charts.Series;
import io.metersphere.reportstatistics.dto.charts.Title;
import io.metersphere.reportstatistics.dto.charts.XAxis;
import io.metersphere.reportstatistics.dto.charts.YAxis;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PieChartDTO {
    private Map<String,String> dataset;
    private Map<String,String> tooltip;
    private XAxis xAxis;
    private YAxis yAxis;
    private List<Series> series;
    private List<Title> title;
    private int width;

    public PieChartDTO() {
        tooltip = new HashMap<>();
    }
}
