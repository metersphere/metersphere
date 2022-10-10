package io.metersphere.reportstatistics.dto.charts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Title {
    private String name;
    private String subtext;
    private String left;
    private String top = "75%";
    private String textAlign = "center";
}
