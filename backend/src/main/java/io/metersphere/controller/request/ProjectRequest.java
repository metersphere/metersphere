package io.metersphere.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    private String workspaceId;
    private String name;
}
