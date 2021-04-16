package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class FieldTemplate implements Serializable {
    private String id;

    private String name;

    private String platform;

    private String description;

    private String scene;

    private String workspaceId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}