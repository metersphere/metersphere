package io.metersphere.api.dto.scenario;

import io.metersphere.request.BodyFile;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class KeyValue {
    private String name;
    private String value;
    private String type;
    private List<BodyFile> files;
    private String description;
    private String contentType;
    private boolean enable = true;
    private boolean urlEncode;
    private boolean required;
    private Integer min;
    private Integer max;

    public KeyValue() {
        this(null, null);
    }

    public KeyValue(String name, String value) {
        this(name, value, null);
    }

    public KeyValue(String name, String value, String description) {
        this(name, value, description, null);
    }

    public KeyValue(String name, String type, String value, boolean required, boolean enable) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.required = required;
        this.enable = enable;
    }

    public KeyValue(String name, String value, String description, String contentType) {
        this(name, value, description, contentType, true);
    }

    public KeyValue(String name, String value, String description, String contentType, boolean required) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.contentType = contentType;
        this.enable = true;
        this.required = required;
    }

    public KeyValue(String name, String value, String description, boolean required) {
        this(name, value, description, StringUtils.EMPTY, required);
    }

    public boolean valueIsNotEmpty() {
       return StringUtils.isNotEmpty(this.getValue());
    }

    public boolean isValid() {
        return (StringUtils.isNotBlank(name) || "JSON-SCHEMA".equals(type)) && !StringUtils.equalsIgnoreCase(type, "file");
    }

    public boolean isFile() {
        return (CollectionUtils.isNotEmpty(files)) && (StringUtils.isEmpty(type) || StringUtils.equalsIgnoreCase(type, "file"));
    }
}
