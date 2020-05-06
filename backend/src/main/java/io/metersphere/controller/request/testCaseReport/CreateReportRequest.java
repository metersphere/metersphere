package io.metersphere.controller.request.testCaseReport;

import lombok.Data;

@Data
public class CreateReportRequest {
    String planId;
    Long templateId;
}
