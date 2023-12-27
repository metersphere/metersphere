package io.metersphere.api.dto.schema;


import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class JsonSchemaItem {
    private Object example;
    private String id;
    private String title;
    private String type = "string";
    private String description;
    private JsonSchemaItem items;
    private Map<String, Object> mock;
    private Map<String, JsonSchemaItem> properties;
    private JsonSchemaItem additionalProperties;
    private List<String> required;
    private String pattern;
    private Integer maxLength;
    private Integer minLength;
    private BigDecimal minimum;
    private BigDecimal maximum;
    private String schema;
    private String format;
    private List<String> enumString;
    private List<Number> enumInteger;
    private List<BigDecimal> enumNumber;
    private Map<String, Object> extensions = null;


    public JsonSchemaItem() {
        this.mock = new LinkedHashMap<>();
        this.mock.put("mock", "");
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
        if (type.equals("object")) {
            this.properties = new LinkedHashMap<>();
        } else if (type.equals("array")) {
            this.items = new JsonSchemaItem();
        }
    }
}
