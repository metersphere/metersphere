package io.metersphere.performance.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteReportRequest {

    private List<String> ids;
}
