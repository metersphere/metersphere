package io.metersphere.reportstatistics.dto.charts;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class XAxis {
    private final String type = "category";
    private List<String> data;
    private String name;
    private Map<String,Integer> axisLabel =  new HashMap<String,Integer>(){ {this.put("interval",0);this.put("rotate",30);}};
}
