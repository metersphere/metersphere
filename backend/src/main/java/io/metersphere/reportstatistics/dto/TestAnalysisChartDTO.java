package io.metersphere.reportstatistics.dto;

import io.metersphere.reportstatistics.dto.charts.Legend;
import io.metersphere.reportstatistics.dto.charts.Series;
import io.metersphere.reportstatistics.dto.charts.XAxis;
import io.metersphere.reportstatistics.dto.charts.YAxis;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestAnalysisChartDTO {
    private Legend legend;
    private XAxis xAxis;
    private YAxis yAxis;
    private List<Series> series;

    public TestAnalysisChartDTO() {
    }

    public TestAnalysisChartDTO(Legend legend, XAxis xAxis, YAxis yAxis, List<Series> series) {
        this.legend = legend;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.series = series;
    }
}
