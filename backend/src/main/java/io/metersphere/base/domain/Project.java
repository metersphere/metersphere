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

    private String caseTemplateId;

    private String issueTemplateId;

    private String createUser;

    private String systemId;

    private String azureFilterId;

    private String platform;

    private Boolean thirdPartTemplate;

    private Boolean versionEnable;

    private String issueConfig;

    private static final long serialVersionUID = 1L;
}