package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.List;

@Data
public class Body {
    private String type;
    private String raw;
    private List<KeyValue> kvs;
}
