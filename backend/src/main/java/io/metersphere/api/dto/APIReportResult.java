package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiTestReport;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class APIReportResult extends ApiTestReport {

    private String testName;

    private String projectName;

    private String userName;

    private List<String> scenarioIds;

    private String content;
}
