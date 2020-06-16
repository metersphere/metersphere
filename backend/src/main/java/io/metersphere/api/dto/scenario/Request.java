package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.scenario.assertions.Assertions;
import io.metersphere.api.dto.scenario.extract.Extract;
import lombok.Data;

import java.util.List;

@Data
public class Request {
    private String name;
    private String url;
    private String method;
    private List<KeyValue> parameters;
    private List<KeyValue> headers;
    private Body body;
    private Assertions assertions;
    private Extract extract;
}
