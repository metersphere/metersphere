package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

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

    private String apiTemplateId;

    private static final long serialVersionUID = 1L;
}