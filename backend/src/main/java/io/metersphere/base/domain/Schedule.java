package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Schedule implements Serializable {
    private String id;

    private String key;

    private String type;

    private String value;

    private String group;

    private String job;

    private Boolean enable;

    private String resourceId;

    private String userId;

    private String workspaceId;

    private Long createTime;

    private Long updateTime;

    private String projectId;

    private String name;

    private String config;

    private static final long serialVersionUID = 1L;
}