package io.metersphere.performance.controller.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteReportRequest {
    private List<String> ids;
    private String projectId;
    private ReportRequest condition;
}
