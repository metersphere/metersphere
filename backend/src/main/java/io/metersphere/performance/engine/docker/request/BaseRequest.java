package io.metersphere.performance.engine.docker.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequest {
    private String testId;
    private String reportId;
}
