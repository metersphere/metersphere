package io.metersphere.environment.dto;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnvironmentRequest extends BaseQueryRequest {

    private String protocol;

    private String projectId;
}
