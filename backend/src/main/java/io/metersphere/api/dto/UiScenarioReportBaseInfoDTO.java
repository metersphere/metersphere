package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UiScenarioReportBaseInfoDTO extends ApiScenarioReportBaseInfoDTO {
    private Boolean isNotStep = false;
    private String uiImg;
    private String reportId;
}
