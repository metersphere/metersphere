package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class JarConfig implements Serializable {
    private String id;

    private String name;

    private String fileName;

    private String creator;

    private String modifier;

    private String path;

    private Boolean enable;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String resourceId;

    private String resourceType;

    private static final long serialVersionUID = 1L;
}