package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO extends LoadTestReportInfoDTO {

    private String content;
    private String projectName;
    private String userName;
    private String versionName;
}
