package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTestReport;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class APIReportResult extends ApiTestReport {

    private String testName;

    private String projectName;

    private String userName;

    private String content;
}
