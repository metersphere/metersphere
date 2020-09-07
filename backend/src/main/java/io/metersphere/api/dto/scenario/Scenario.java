package io.metersphere.api.dto.scenario;

import io.metersphere.api.dto.scenario.request.Request;
import lombok.Data;

import java.util.List;

@Data
public class Scenario {
    private String id;
    private String name;
    private String url;
    private String environmentId;
    private Boolean enableCookieShare;
    private List<KeyValue> variables;
    private List<KeyValue> headers;
    private List<Request> requests;
    private DubboConfig dubboConfig;
    private Boolean enable;
}
