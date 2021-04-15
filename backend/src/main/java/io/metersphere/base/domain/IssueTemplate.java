package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class IssueTemplate implements Serializable {
    private String id;

    private String name;

    private String platform;

    private String description;

    private String title;

    private Boolean system;

    private Boolean global;

    private String workspaceId;

    private Long createTime;

    private Long updateTime;

    private String content;

    private static final long serialVersionUID = 1L;
}