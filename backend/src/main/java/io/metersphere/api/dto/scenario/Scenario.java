package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.List;

@Data
public class Scenario {
    private String name;
    private String url;
    private String environmentId;
    private List<KeyValue> variables;
    private List<KeyValue> headers;
    private List<Request> requests;
}
