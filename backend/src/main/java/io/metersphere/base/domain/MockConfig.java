package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class MockConfig implements Serializable {
    private String id;

    private String projectId;

    private String apiId;

    private String apiPath;

    private String apiMethod;

    private Long createTime;

    private Long updateTime;

    private String createUserId;

    private static final long serialVersionUID = 1L;

}