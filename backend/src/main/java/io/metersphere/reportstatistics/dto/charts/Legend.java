package io.metersphere.reportstatistics.dto.charts;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Legend {
    private final String x = "center";
    private String y = "bottom";
    private final String type = "scroll";
    private final List<Integer> padding = Arrays.asList(0, 40, 0, 0);
    private Map<String, Boolean> selected;
    private List<String> data;

}
