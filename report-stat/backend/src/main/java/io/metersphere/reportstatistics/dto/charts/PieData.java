package io.metersphere.reportstatistics.dto.charts;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PieData {
    private String name;
    private long value;
    private Map<String,String> itemStyle;

    public void setColor(String color){
        if(itemStyle == null){
            itemStyle = new HashMap<>(0);
        }
        itemStyle.put("color",color);
    }
}
