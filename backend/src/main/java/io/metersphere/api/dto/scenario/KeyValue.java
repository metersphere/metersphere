package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.scenario.request.BodyFile;
import lombok.Data;

import java.util.List;

@Data
public class KeyValue {
    private String name;
    private String value;
    private String type;
    private List<BodyFile> files;
    private String description;
    private boolean enable;

    public KeyValue() {
    }

    public KeyValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public KeyValue(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
