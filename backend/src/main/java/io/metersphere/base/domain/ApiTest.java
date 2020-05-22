package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class ApiTest implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private String status;

    private String userId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}