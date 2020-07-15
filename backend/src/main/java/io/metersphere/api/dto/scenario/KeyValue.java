package io.metersphere.api.dto.scenario;

import lombok.Data;

@Data
public class KeyValue {
    private String name;
    private String value;
    private String description;

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
