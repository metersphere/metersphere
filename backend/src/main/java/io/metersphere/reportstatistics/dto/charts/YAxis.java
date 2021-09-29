package io.metersphere.reportstatistics.dto.charts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YAxis {
    private String type;
    private List<String> data;
    private String name;
}
