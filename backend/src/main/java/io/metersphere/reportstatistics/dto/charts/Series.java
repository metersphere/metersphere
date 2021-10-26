package io.metersphere.reportstatistics.dto.charts;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Series {
    private String name;
    private List<Object> data;
    private String color = "#783887";
    private String type = "line";
    private String radius = "50";
    private String stack;
    private JSONObject encode;
    private List<String> center;
    private String barWidth = null;
    private Map<String,Object> label;
}
