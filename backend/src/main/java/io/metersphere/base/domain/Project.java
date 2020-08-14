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

    private static final long serialVersionUID = 1L;
}