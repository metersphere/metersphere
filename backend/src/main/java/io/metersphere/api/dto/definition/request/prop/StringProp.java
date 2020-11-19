package io.metersphere.api.dto.definition.request.prop;

import lombok.Data;

@Data
public class StringProp {
    private String id;
    private String key;
    private String name;
    private String type;
    private String value;
}
