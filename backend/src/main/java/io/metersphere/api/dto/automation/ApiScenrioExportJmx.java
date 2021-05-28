package io.metersphere.api.dto.automation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiScenrioExportJmx {
    private String name;
    private String jmx;
    List<String> files;

    public ApiScenrioExportJmx(String name, String jmx) {
        this.name = name;
        this.jmx = jmx;
    }
}
