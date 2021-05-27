package io.metersphere.log.vo;

import lombok.Data;

import java.util.UUID;

@Data
public class DetailColumn {
    private String id;
    private boolean depthDff;
    private String columnTitle;
    private String columnName;
    private Object originalValue;
    private Object newValue;

    public DetailColumn() {

    }

    public DetailColumn(String columnTitle, String columnName, Object originalValue, Object newValue) {
        this.id = UUID.randomUUID().toString();
        this.columnTitle = columnTitle;
        this.columnName = columnName;
        this.originalValue = originalValue;
        this.newValue = newValue;
    }
}
