package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinitionEnv implements Serializable {
    private String id;

    private String userId;

    private String envId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}