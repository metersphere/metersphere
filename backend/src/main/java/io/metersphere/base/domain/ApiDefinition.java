package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiDefinition implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String method;

    private String url;

    private String environmentId;

    private String status;

    private String description;

    private String userId;

    private String moduleId;

    private String modulePath;

    private Long createTime;

    private Long updateTime;

    private String request;

    private String response;

    private static final long serialVersionUID = 1L;
}