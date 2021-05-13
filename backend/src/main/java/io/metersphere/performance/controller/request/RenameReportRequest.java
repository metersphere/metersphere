package io.metersphere.performance.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenameReportRequest {
    private String name;
    private String id;
}
