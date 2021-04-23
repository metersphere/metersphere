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

    private Integer sort;

    private String projectId;

    private String fileName;

    private String creator;

    private String modifier;

    private String method;

    private String path;

    private String description;

    private static final long serialVersionUID = 1L;
}