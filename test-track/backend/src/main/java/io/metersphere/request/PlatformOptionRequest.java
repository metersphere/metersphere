package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatformOptionRequest extends IntegrationRequest {
    private String optionMethod;
    private String projectConfig;
    private String projectId;
    private String query;
}
