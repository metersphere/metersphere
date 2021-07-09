package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class Workspace implements Serializable {
    private String id;

    private String organizationId;

    private String name;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private static final long serialVersionUID = 1L;
}