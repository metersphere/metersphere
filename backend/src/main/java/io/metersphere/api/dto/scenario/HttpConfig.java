package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.List;

@Data
public class HttpConfig {
    private String socket;
    private String domain;
    private String protocol = "https";
    private int port;
    private List<KeyValue> headers;
}
