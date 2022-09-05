package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class FileMetadata implements Serializable {
    private String id;

    private String name;

    private String type;

    private Long size;

    private Long createTime;

    private Long updateTime;

    private String projectId;

    private String storage;

    private String createUser;

    private String updateUser;

    private String tags;

    private String moduleId;

    private Boolean loadJar;

    private String path;

    private String resourceType;

    private Boolean latest;

    private String refId;

    private static final long serialVersionUID = 1L;
}