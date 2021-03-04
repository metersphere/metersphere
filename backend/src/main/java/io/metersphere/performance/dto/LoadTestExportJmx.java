package io.metersphere.performance.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoadTestExportJmx {
    private String name;
    private String jmx;

    public LoadTestExportJmx(String name, String jmx) {
        this.name = name;
        this.jmx = jmx;
    }
}
