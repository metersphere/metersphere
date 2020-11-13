package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.List;

@Data
public class Body {
    private String type;
    private String raw;
    private String format;
    private Object json;
    private String xml;
    private List<KeyValue> kvs;
}
