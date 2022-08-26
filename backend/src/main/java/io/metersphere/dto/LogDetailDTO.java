package io.metersphere.dto;

import io.metersphere.base.domain.LoadTestReportLog;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LogDetailDTO {
    private String resourceId;
    private String resourceName;
    private String content;
    private List<LoadTestReportLog> reportLogs;
}
