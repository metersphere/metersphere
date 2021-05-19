package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTest implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String testResourcePoolId;

    private String userId;

    private Integer num;

    private String createUser;

    private static final long serialVersionUID = 1L;
}