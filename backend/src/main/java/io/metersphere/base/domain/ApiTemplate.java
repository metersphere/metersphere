package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String description;
    private Boolean system;
    private Boolean global;
    private Long createTime;
    private Long updateTime;
    private String createUser;
    private String projectId;
}