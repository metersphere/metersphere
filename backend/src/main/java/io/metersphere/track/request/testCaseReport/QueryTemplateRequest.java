package io.metersphere.track.request.testCaseReport;

import io.metersphere.base.domain.TestCaseReportTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTemplateRequest extends TestCaseReportTemplate {
    Boolean queryDefault;
}
