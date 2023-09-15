package io.metersphere.system.base.param;

import lombok.Data;

@Data
public class InvalidateParamInfo {
    private String name;
    private Object value;
    private Class annotation;

    public InvalidateParamInfo(String name, Object value, Class annotation) {
        this.name = name;
        this.value = value;
        this.annotation = annotation;
    }
}
