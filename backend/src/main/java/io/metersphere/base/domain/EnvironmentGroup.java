package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

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