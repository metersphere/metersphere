package io.metersphere.api.dto.scenario;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class Body {
    private String type;
    private String raw;
    private String format;
    private List<KeyValue> kvs;

    private final static String KV = "KeyValue";
    private final static String FORM_DATA = "Form Data";
    private final static String RAW = "Raw";

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
}
