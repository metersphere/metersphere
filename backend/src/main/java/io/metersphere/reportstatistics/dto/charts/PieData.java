package io.metersphere.reportstatistics.dto.charts;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PieData {
    private String name;
    private long value;
    private JSONObject itemStyle;

    public void setColor(String color){
        if(itemStyle == null){
            itemStyle = new JSONObject();
        }
        itemStyle.put("color",color);
    }
}
