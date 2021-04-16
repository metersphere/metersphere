package io.metersphere.api.dto.automation;

import io.metersphere.base.domain.ApiTestReport;
import lombok.Data;

@Data
public class ApiTestReportVariable extends ApiTestReport {
    public String executionTime;
    public String executor;
    public String executionEnvironment;
}
