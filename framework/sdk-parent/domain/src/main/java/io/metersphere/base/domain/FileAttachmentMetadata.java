package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileAttachmentMetadata implements Serializable {
    private String id;

    private String name;

    private String type;

    private Long size;

    private Long createTime;

    private Long updateTime;

    private String creator;

    private String filePath;

    private Boolean isLocal;

    private Boolean isRelatedDeleted;

    private static final long serialVersionUID = 1L;
}