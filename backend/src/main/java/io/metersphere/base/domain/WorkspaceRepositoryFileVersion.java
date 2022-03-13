package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class WorkspaceRepositoryFileVersion implements Serializable {
    private String id;

    private String repositoryId;

    private String branch;

    private String path;

    private String scenarioId;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private String commitId;

    private static final long serialVersionUID = 1L;
}