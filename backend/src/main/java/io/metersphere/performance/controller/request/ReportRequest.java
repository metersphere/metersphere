package io.metersphere.performance.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private String name;
    private String workspaceId;
}
