package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class WorkspaceRepository implements Serializable {
    private String id;

    private String repositoryName;

    private String repositoryUrl;

    private String username;

    private String password;

    private Long createTime;

    private Long updateTime;

    private String workspaceId;

    private String createUser;

    private String description;

    private static final long serialVersionUID = 1L;
}