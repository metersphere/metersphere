package io.metersphere.load.domain;

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

    private Integer num;

    private String createUser;

    private Long pos;

    private String versionId;

    private String refId;

    private Boolean latest;

    private static final long serialVersionUID = 1L;
}