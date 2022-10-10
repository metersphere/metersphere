package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author song.tianyang
 * @Date 2022/4/9 3:50 下午
 */
@Getter
@Setter
public class ApiScenarioReportBaseInfoDTO {
    private String reqName;
    private boolean reqSuccess;
    private int reqError;
    private long reqStartTime;
    private String rspCode;
    private long rspTime;
    private String uiImg;
    private String uiScreenshots;
    private String combinationImg;
    private Boolean isNotStep;
    private String reportId;
}
