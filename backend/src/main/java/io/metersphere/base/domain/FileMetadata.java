package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class FileMetadata implements Serializable {
    private String id;

    private String name;

    private String type;

    private Long createTime;

    private Long updateTime;

    private Long size;

    private String projectId;

    private static final long serialVersionUID = 1L;
}