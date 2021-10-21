package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntegrationRequest {
    private String platform;
    private String workspaceId;
}
