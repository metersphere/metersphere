package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTestJarConfig implements Serializable {
    private String id;

    private String name;

    private String fileName;

    private String owner;

    private String path;

    private String projectId;

    private String description;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}