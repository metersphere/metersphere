package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

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

    private static final long serialVersionUID = 1L;
}