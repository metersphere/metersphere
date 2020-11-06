package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDelimit implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String path;

    private String url;

    private String description;

    private String status;

    private String userId;

    private String moduleId;

    private String modulePath;

    private Long createTime;

    private Long updateTime;

    private String request;

    private static final long serialVersionUID = 1L;
}