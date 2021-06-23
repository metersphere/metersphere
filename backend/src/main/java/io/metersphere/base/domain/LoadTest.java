package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

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

    private Integer scenarioVersion;

    private String scenarioId;

    private static final long serialVersionUID = 1L;
}