package io.metersphere.controller.request;

import io.metersphere.base.domain.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProjectRequest extends Project {
    private String protocal;

    private Boolean customNum;
    private Boolean scenarioCustomNum;
    private Integer mockTcpPort;
    private Boolean isMockTcpOpen;
    private Boolean casePublic;
    private String apiQuick;
    private Boolean cleanTrackReport;
    private String cleanTrackReportExpr;
    private Boolean cleanApiReport;
    private String cleanApiReportExpr;
    private Boolean cleanLoadReport;
    private String cleanLoadReportExpr;
}
