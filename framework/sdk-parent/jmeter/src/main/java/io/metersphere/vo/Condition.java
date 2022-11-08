package io.metersphere.vo;

import lombok.Data;

@Data
public class Condition {
    private String key;
    private Object value;

    public Condition() {

    }

    public Condition(String key, Object value) {
        this.key = key;
        this.value = value;
    }
}
