package io.metersphere.api.dto.scenario;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class Body {
    private String type;
    private String raw;
    private String format;
    private List<KeyValue> fromUrlencoded;
    private List<KeyValue> kvs;
    private List<KeyValue> binary;
    private Object json;
    private String xml;

    private final static String KV = "KeyValue";
    private final static String FORM_DATA = "Form Data";
    private final static String RAW = "Raw";
    private final static String BINARY = "Binary";
    private final static String JSON = "JSON";
    private final static String XML = "XML";

    public boolean isValid() {
        if (this.isKV()) {
            return kvs.stream().anyMatch(KeyValue::isValid);
        } else {
            return StringUtils.isNotBlank(raw);
        }
    }

    public boolean isKV() {
        return StringUtils.equals(type, KV);
    }

    public boolean isBinary() {
        return StringUtils.equals(type, BINARY);
    }

    public boolean isJson() {
        return StringUtils.equals(type, JSON);
    }

    public boolean isXml() {
        return StringUtils.equals(type, XML);
    }

}
