package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiScenrioExportJmx {
    private String name;
    private String jmx;

    public ApiScenrioExportJmx(String name, String jmx) {
        this.name = name;
        this.jmx = jmx;
    }
}
