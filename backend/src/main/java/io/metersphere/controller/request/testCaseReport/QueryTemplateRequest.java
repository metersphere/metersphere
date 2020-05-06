package io.metersphere.controller.request.testCaseReport;

import io.metersphere.base.domain.TestCaseReportTemplate;
import lombok.Data;

@Data
public class QueryTemplateRequest extends TestCaseReportTemplate {
    Boolean queryDefault;
}
