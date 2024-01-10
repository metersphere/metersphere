package io.metersphere.bug.dto;

import lombok.Data;

@Data
public class BugTemplateInjectField {
    private String id;
    private String name;
    private String key;
    private String type;
    private String defaultValue;
    private Boolean required;
    private Boolean supportSearch;
    private String optionMethod;
}
