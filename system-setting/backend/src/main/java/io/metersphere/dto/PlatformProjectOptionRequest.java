package io.metersphere.dto;

import io.metersphere.request.IntegrationRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformProjectOptionRequest extends IntegrationRequest {
    private String optionMethod;
    private String projectConfig;
}
