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

    private static final long serialVersionUID = 1L;
}