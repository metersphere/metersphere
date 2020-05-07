package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiTest implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private String status;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}