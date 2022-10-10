package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Group implements Serializable {
    private String id;

    private String name;

    private String description;

    private Boolean system;

    private String type;

    private Long createTime;

    private Long updateTime;

    private String creator;

    private String scopeId;

    private static final long serialVersionUID = 1L;
}