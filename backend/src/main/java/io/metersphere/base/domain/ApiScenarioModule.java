package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiScenarioModule implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String userId;

    private String parentId;

    private String level;

    private Long createTime;

    private Long updateTime;

}