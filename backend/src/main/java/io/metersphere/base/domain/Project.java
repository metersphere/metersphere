package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Project implements Serializable {
    private String id;

    private String workspaceId;

    private String name;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String tapdId;

    private String jiraKey;

    private String zentaoId;

    private String azureDevopsId;

    private Boolean repeatable;

    private String caseTemplateId;

    private String issueTemplateId;

    private Boolean customNum;

    private Boolean scenarioCustomNum;

    private String createUser;

    private String systemId;

    private Integer mockTcpPort;

    private Boolean isMockTcpOpen;

    private String azureFilterId;

    private String apiQuick;

    private Boolean casePublic;

    private String platform;

    private Boolean thirdPartTemplate;

    private Boolean versionEnable;

    private Boolean cleanTrackReport;

    private String cleanTrackReportExpr;

    private Boolean cleanApiReport;

    private String cleanApiReportExpr;

    private Boolean cleanLoadReport;

    private String cleanLoadReportExpr;

    private static final long serialVersionUID = 1L;
}