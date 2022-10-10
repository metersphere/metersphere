package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class EnvironmentGroup implements Serializable {
    private String id;

    private String name;

    private String workspaceId;

    private String description;

    private String createUser;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}