package io.metersphere.reportstatistics.dto;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.reportstatistics.dto.charts.Series;
import io.metersphere.reportstatistics.dto.charts.Title;
import io.metersphere.reportstatistics.dto.charts.XAxis;
import io.metersphere.reportstatistics.dto.charts.YAxis;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PieChartDTO {
    private JSONObject dataset;
    private JSONObject tooltip;
    private XAxis xAxis;
    private YAxis yAxis;
    private List<Series> series;
    private List<Title> title;
    private int width;

    public PieChartDTO() {
        tooltip = new JSONObject();
    }
}
