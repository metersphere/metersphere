package io.metersphere.api.dto.definition.request.variable;

import io.metersphere.commons.constants.PropertyConstant;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JsonSchemaItem {
    private String id;
    private String title;
    private String type;
    private String description;
    private List<JsonSchemaItem> items;
    private Map<String, Object> mock;
    private Map<String, JsonSchemaItem> properties;
    private JsonSchemaItem additionalProperties;
    private List<String> required;
    private String pattern;
    private Integer maxLength;
    private Integer minLength;
    private String schema;

    public JsonSchemaItem() {
        this.mock = new LinkedHashMap<>();
        this.mock.put(PropertyConstant.MOCK, "");
    }

    public JsonSchemaItem(String type) {
        this.type = type;
        this.initParam(type);
    }

    public void setType(String type) {
        this.type = type;
        this.initParam(type);
    }

    private void initParam(String type) {
        if (type.equals(PropertyConstant.OBJECT)) {
            this.properties = new LinkedHashMap<>();
        } else if (type.equals(PropertyConstant.ARRAY)) {
            this.items = new LinkedList<>();
        }
    }
}
